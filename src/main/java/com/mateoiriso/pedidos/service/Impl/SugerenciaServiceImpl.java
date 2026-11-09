package com.mateoiriso.pedidos.service.Impl;

import com.mateoiriso.pedidos.exceptions.IaNoDisponibleException;
import com.mateoiriso.pedidos.exceptions.NotFoundException;
import com.mateoiriso.pedidos.model.dto.entrega.SugerenciaSobranteDto;
import com.mateoiriso.pedidos.model.dto.entrega.SugerenciasDto;
import com.mateoiriso.pedidos.model.entity.Pedido;
import com.mateoiriso.pedidos.model.entity.PedidoDetalle;
import com.mateoiriso.pedidos.model.entity.Sobrante;
import com.mateoiriso.pedidos.model.enums.EEstadoPedido;
import com.mateoiriso.pedidos.model.repository.PedidoDetalleRepository;
import com.mateoiriso.pedidos.model.repository.PedidoRepository;
import com.mateoiriso.pedidos.model.repository.RepartoRepository;
import com.mateoiriso.pedidos.model.repository.SobranteRepository;
import com.mateoiriso.pedidos.service.ISugerenciaService;
import com.mateoiriso.pedidos.service.ia.ISugerenciaIaClient;
import com.mateoiriso.pedidos.service.ia.RespuestaSugerenciasIa;
import com.mateoiriso.pedidos.service.ia.RespuestaSugerenciasIa.SugerenciaIa;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SugerenciaServiceImpl implements ISugerenciaService {

    private final RepartoRepository repartoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository detalleRepository;
    private final SobranteRepository sobranteRepository;
    /** Opcional: si la IA no está habilitada, no hay bean y el endpoint responde 503. */
    private final ObjectProvider<ISugerenciaIaClient> iaClient;

    @Override
    @Transactional(readOnly = true)
    public SugerenciasDto sugerirSobrantes(Integer repartoId) {
        if (!repartoRepository.existsById(repartoId)) {
            throw new NotFoundException("No existe el reparto " + repartoId);
        }
        ISugerenciaIaClient ia = iaClient.getIfAvailable();
        if (ia == null) {
            throw new IaNoDisponibleException("Las sugerencias con IA no están habilitadas en este servidor");
        }

        Map<Integer, Sobrante> sobrantes = sobranteRepository.findByRepartoId(repartoId).stream()
                .collect(Collectors.toMap(s -> s.producto.id, Function.identity()));
        Map<Integer, Pedido> pendientes = pedidoRepository.findByRepartoId(repartoId).stream()
                .filter(p -> p.estado == EEstadoPedido.ASIGNADO)
                .collect(Collectors.toMap(p -> p.id, Function.identity()));
        // Sin sobrantes o sin pedidos por entregar no hay nada que sugerir: no se llama a la IA (ahorra costo).
        if (sobrantes.isEmpty() || pendientes.isEmpty()) {
            return new SugerenciasDto(repartoId, ia.modelo(), List.of(), List.of());
        }
        Map<Integer, List<PedidoDetalle>> detalles = new LinkedHashMap<>();
        pendientes.keySet().forEach(id -> detalles.put(id, detalleRepository.findByPedidoId(id)));

        RespuestaSugerenciasIa respuesta = ia.sugerir(describirReparto(sobrantes, detalles));
        return validar(repartoId, ia.modelo(), respuesta, sobrantes, pendientes, detalles);
    }

    /**
     * Arma el contexto que recibe el modelo. Solo viajan IDs y productos:
     * nombres, emails y teléfonos de los clientes no se envían a la IA.
     */
    String describirReparto(Map<Integer, Sobrante> sobrantes, Map<Integer, List<PedidoDetalle>> detalles) {
        StringBuilder sb = new StringBuilder("Sobrantes en la camioneta:\n");
        sobrantes.values().forEach(s -> sb.append("- productoId ").append(s.producto.id)
                .append(" (").append(s.producto.nombre).append("): ").append(s.cantidad).append(" unidades\n"));
        sb.append("\nPedidos del reparto que todavía no se entregaron:\n");
        detalles.forEach((pedidoId, lineas) -> {
            sb.append("- pedidoId ").append(pedidoId).append(": ");
            sb.append(lineas.stream()
                    .map(d -> d.producto.nombre + " (productoId " + d.producto.id + ") x" + d.cantidad)
                    .collect(Collectors.joining(", ")));
            sb.append('\n');
        });
        return sb.toString();
    }

    /** Lo que devuelve la IA es una propuesta: cada sugerencia se verifica contra la base antes de mostrarse. */
    SugerenciasDto validar(Integer repartoId, String modelo, RespuestaSugerenciasIa respuesta,
                           Map<Integer, Sobrante> sobrantes, Map<Integer, Pedido> pendientes,
                           Map<Integer, List<PedidoDetalle>> detalles) {
        Map<Integer, Integer> disponibles = new HashMap<>();
        sobrantes.forEach((productoId, s) -> disponibles.put(productoId, s.cantidad));
        List<SugerenciaSobranteDto> validas = new ArrayList<>();
        List<String> descartadas = new ArrayList<>();

        List<SugerenciaIa> propuestas = respuesta == null || respuesta.sugerencias() == null
                ? List.of() : respuesta.sugerencias();
        for (SugerenciaIa s : propuestas) {
            Pedido pedido = pendientes.get(s.pedidoId());
            Sobrante sobrante = sobrantes.get(s.productoId());
            if (pedido == null) {
                descartadas.add("Pedido " + s.pedidoId() + ": no está pendiente de entrega en este reparto");
                continue;
            }
            if (sobrante == null) {
                descartadas.add("Producto " + s.productoId() + ": no hay sobrantes de ese producto");
                continue;
            }
            int restante = disponibles.get(s.productoId());
            if (s.cantidad() <= 0 || s.cantidad() > restante) {
                descartadas.add("Pedido " + s.pedidoId() + ", " + sobrante.producto.nombre + ": se sugirieron "
                        + s.cantidad() + " unidades y quedan " + restante);
                continue;
            }
            disponibles.put(s.productoId(), restante - s.cantidad());

            // El precio no lo decide la IA: se aplica la misma regla que al agregar el sobrante.
            Optional<PedidoDetalle> original = detalles.get(pedido.id).stream()
                    .filter(d -> Objects.equals(d.producto.id, s.productoId()))
                    .findFirst();
            validas.add(SugerenciaSobranteDto.builder()
                    .pedidoId(pedido.id)
                    .cliente(pedido.cliente.nombre)
                    .productoId(s.productoId())
                    .producto(sobrante.producto.nombre)
                    .cantidad(s.cantidad())
                    .precioUnitario(original.map(d -> d.precioUnitario).orElse(sobrante.producto.precio))
                    .respetaPrecioOriginal(original.isPresent())
                    .motivo(s.motivo())
                    .build());
        }
        return new SugerenciasDto(repartoId, modelo, validas, descartadas);
    }
}
