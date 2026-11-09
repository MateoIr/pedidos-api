package com.mateoiriso.pedidos.service.Impl;

import com.mateoiriso.pedidos.exceptions.BusinessException;
import com.mateoiriso.pedidos.exceptions.NotFoundException;
import com.mateoiriso.pedidos.model.dto.entrega.RequestEntregaDto;
import com.mateoiriso.pedidos.model.dto.entrega.RequestSobranteDto;
import com.mateoiriso.pedidos.model.dto.entrega.SobranteDto;
import com.mateoiriso.pedidos.model.dto.pedido.PedidoDto;
import com.mateoiriso.pedidos.model.entity.*;
import com.mateoiriso.pedidos.model.enums.EEstadoPedido;
import com.mateoiriso.pedidos.model.enums.EEstadoReparto;
import com.mateoiriso.pedidos.model.mappers.entrega.SobranteMapper;
import com.mateoiriso.pedidos.model.repository.*;
import com.mateoiriso.pedidos.service.IEntregaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntregaServiceImpl implements IEntregaService {

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository detalleRepository;
    private final SobranteRepository sobranteRepository;
    private final ProductoRepository productoRepository;
    private final RepartoRepository repartoRepository;
    private final SobranteMapper sobranteMapper;
    private final PedidoServiceImpl pedidoService;

    @Override
    @Transactional
    public PedidoDto registrarEntrega(Integer pedidoId, RequestEntregaDto dto) {
        Pedido pedido = buscarPedidoEnReparto(pedidoId);
        Map<Integer, PedidoDetalle> detalles = detalleRepository.findByPedidoId(pedidoId).stream()
                .collect(Collectors.toMap(d -> d.id, Function.identity()));
        Map<Integer, Integer> informadas = dto.items.stream()
                .collect(Collectors.toMap(i -> i.detalleId, i -> i.cantidadEntregada, (a, b) -> {
                    throw new BusinessException("Hay una línea del pedido informada dos veces");
                }));
        if (!informadas.keySet().equals(detalles.keySet())) {
            throw new BusinessException("Hay que informar la entrega de todas las líneas del pedido, y solo de ese pedido");
        }

        boolean completo = true;
        boolean algoEntregado = false;
        for (PedidoDetalle detalle : detalles.values()) {
            int entregada = informadas.get(detalle.id);
            if (entregada > detalle.cantidad) {
                throw new BusinessException("No se pueden entregar más unidades de " + detalle.producto.nombre + " que las pedidas");
            }
            detalle.cantidadEntregada = entregada;
            detalleRepository.save(detalle);
            sumarSobrante(pedido.reparto, detalle.producto, detalle.cantidad - entregada);
            completo &= entregada == detalle.cantidad;
            algoEntregado |= entregada > 0;
        }

        pedido.estado = completo ? EEstadoPedido.ENTREGADO
                : algoEntregado ? EEstadoPedido.PARCIAL
                : EEstadoPedido.CANCELADO;
        return pedidoService.toDto(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoDto cancelar(Integer pedidoId) {
        Pedido pedido = buscarPedidoEnReparto(pedidoId);
        for (PedidoDetalle detalle : detalleRepository.findByPedidoId(pedidoId)) {
            detalle.cantidadEntregada = 0;
            detalleRepository.save(detalle);
            sumarSobrante(pedido.reparto, detalle.producto, detalle.cantidad);
        }
        pedido.estado = EEstadoPedido.CANCELADO;
        return pedidoService.toDto(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoDto agregarSobrante(Integer pedidoId, RequestSobranteDto dto) {
        Pedido pedido = buscarPedidoEnReparto(pedidoId);
        Sobrante sobrante = sobranteRepository.findByRepartoIdAndProductoId(pedido.reparto.id, dto.productoId)
                .orElseThrow(() -> new BusinessException("No hay sobrantes de ese producto en este reparto"));
        if (sobrante.cantidad < dto.cantidad) {
            throw new BusinessException("Solo quedan " + sobrante.cantidad + " unidades de " + sobrante.producto.nombre);
        }

        Optional<PedidoDetalle> existente = detalleRepository.findByPedidoIdAndProductoId(pedidoId, dto.productoId);
        if (existente.isPresent()) {
            // Regla 1: el producto ya estaba en el pedido -> se respeta el precio original.
            PedidoDetalle detalle = existente.get();
            detalle.cantidad += dto.cantidad;
            detalleRepository.save(detalle);
        } else {
            // Regla 2: el producto no estaba -> se cobra el precio vigente (que puede haber aumentado).
            Producto producto = productoRepository.findById(dto.productoId)
                    .orElseThrow(() -> new NotFoundException("No existe el producto " + dto.productoId));
            detalleRepository.save(new PedidoDetalle(null, pedido, producto, dto.cantidad, producto.precio, null, true));
        }

        sobrante.cantidad -= dto.cantidad;
        if (sobrante.cantidad == 0) {
            sobranteRepository.delete(sobrante);
        } else {
            sobranteRepository.save(sobrante);
        }
        return pedidoService.toDto(pedido);
    }

    @Override
    public List<SobranteDto> sobrantesDelReparto(Integer repartoId) {
        if (!repartoRepository.existsById(repartoId)) {
            throw new NotFoundException("No existe el reparto " + repartoId);
        }
        return sobranteRepository.findByRepartoId(repartoId).stream()
                .map(sobranteMapper::entityToDto)
                .toList();
    }

    /** El pedido tiene que estar asignado a un reparto abierto y todavía sin entregar. */
    private Pedido buscarPedidoEnReparto(Integer pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("No existe el pedido " + pedidoId));
        if (pedido.estado != EEstadoPedido.ASIGNADO || pedido.reparto == null) {
            throw new BusinessException("El pedido " + pedidoId + " no está en reparto");
        }
        if (pedido.reparto.estado != EEstadoReparto.ABIERTO) {
            throw new BusinessException("El reparto del pedido ya está cerrado");
        }
        return pedido;
    }

    private void sumarSobrante(Reparto reparto, Producto producto, int cantidad) {
        if (cantidad <= 0) {
            return;
        }
        Sobrante sobrante = sobranteRepository.findByRepartoIdAndProductoId(reparto.id, producto.id)
                .orElseGet(() -> new Sobrante(null, reparto, producto, 0));
        sobrante.cantidad += cantidad;
        sobranteRepository.save(sobrante);
    }
}
