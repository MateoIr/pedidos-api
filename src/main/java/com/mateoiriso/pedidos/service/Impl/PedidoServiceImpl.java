package com.mateoiriso.pedidos.service.Impl;

import com.mateoiriso.pedidos.exceptions.BusinessException;
import com.mateoiriso.pedidos.exceptions.NotFoundException;
import com.mateoiriso.pedidos.model.dto.pedido.PedidoDetalleDto;
import com.mateoiriso.pedidos.model.dto.pedido.PedidoDto;
import com.mateoiriso.pedidos.model.dto.pedido.RequestItemDto;
import com.mateoiriso.pedidos.model.dto.pedido.RequestPedidoDto;
import com.mateoiriso.pedidos.model.entity.*;
import com.mateoiriso.pedidos.model.enums.EEstadoPedido;
import com.mateoiriso.pedidos.model.enums.ERol;
import com.mateoiriso.pedidos.model.mappers.pedido.PedidoDetalleMapper;
import com.mateoiriso.pedidos.model.mappers.pedido.PedidoMapper;
import com.mateoiriso.pedidos.model.repository.*;
import com.mateoiriso.pedidos.service.IPedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository detalleRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ZonaRepository zonaRepository;
    private final PedidoMapper pedidoMapper;
    private final PedidoDetalleMapper detalleMapper;

    @Override
    @Transactional
    public PedidoDto crear(Integer clienteId, RequestPedidoDto dto) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("No existe el usuario " + clienteId));
        if (cliente.rol != ERol.CLIENTE) {
            throw new BusinessException("Solo un cliente puede hacer pedidos");
        }
        Zona zona = zonaRepository.findById(dto.zonaId)
                .orElseThrow(() -> new NotFoundException("No existe la zona " + dto.zonaId));
        validarSinProductosRepetidos(dto.items);

        Pedido pedido = new Pedido(null, cliente, zona, null, EEstadoPedido.PENDIENTE, LocalDateTime.now());
        pedido = pedidoRepository.save(pedido);

        for (RequestItemDto item : dto.items) {
            Producto producto = productoRepository.findById(item.productoId)
                    .orElseThrow(() -> new NotFoundException("No existe el producto " + item.productoId));
            if (!producto.activo) {
                throw new BusinessException("El producto " + producto.nombre + " no está disponible");
            }
            // El precio se copia al detalle: futuros aumentos no cambian este pedido.
            detalleRepository.save(new PedidoDetalle(null, pedido, producto, item.cantidad, producto.precio, null, false));
        }
        return toDto(pedido);
    }

    @Override
    public PedidoDto findById(Integer id) {
        return toDto(pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el pedido " + id)));
    }

    @Override
    public List<PedidoDto> findByCliente(Integer clienteId) {
        return pedidoRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId).stream()
                .map(this::toDto)
                .toList();
    }

    /** Arma el DTO con sus detalles y el total. Lo reusan otros servicios. */
    PedidoDto toDto(Pedido pedido) {
        PedidoDto dto = pedidoMapper.entityToDto(pedido);
        List<PedidoDetalleDto> detalles = detalleRepository.findByPedidoId(pedido.id).stream()
                .map(detalleMapper::entityToDto)
                .toList();
        dto.detalles = detalles;
        dto.total = detalles.stream().map(d -> d.subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return dto;
    }

    private void validarSinProductosRepetidos(List<RequestItemDto> items) {
        Set<Integer> vistos = new HashSet<>();
        for (RequestItemDto item : items) {
            if (!vistos.add(item.productoId)) {
                throw new BusinessException("El producto " + item.productoId + " está repetido en el pedido");
            }
        }
    }
}
