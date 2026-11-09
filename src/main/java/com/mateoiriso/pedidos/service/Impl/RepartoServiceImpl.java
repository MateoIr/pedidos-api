package com.mateoiriso.pedidos.service.Impl;

import com.mateoiriso.pedidos.exceptions.BusinessException;
import com.mateoiriso.pedidos.exceptions.NotFoundException;
import com.mateoiriso.pedidos.model.dto.pedido.PedidoDto;
import com.mateoiriso.pedidos.model.dto.reparto.RepartoDto;
import com.mateoiriso.pedidos.model.dto.reparto.RequestRepartoDto;
import com.mateoiriso.pedidos.model.entity.Pedido;
import com.mateoiriso.pedidos.model.entity.Reparto;
import com.mateoiriso.pedidos.model.entity.Usuario;
import com.mateoiriso.pedidos.model.entity.Zona;
import com.mateoiriso.pedidos.model.enums.EEstadoPedido;
import com.mateoiriso.pedidos.model.enums.EEstadoReparto;
import com.mateoiriso.pedidos.model.enums.ERol;
import com.mateoiriso.pedidos.model.mappers.reparto.RepartoMapper;
import com.mateoiriso.pedidos.model.repository.PedidoRepository;
import com.mateoiriso.pedidos.model.repository.RepartoRepository;
import com.mateoiriso.pedidos.model.repository.UsuarioRepository;
import com.mateoiriso.pedidos.model.repository.ZonaRepository;
import com.mateoiriso.pedidos.service.IRepartoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RepartoServiceImpl implements IRepartoService {

    private final RepartoRepository repartoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ZonaRepository zonaRepository;
    private final RepartoMapper repartoMapper;
    private final PedidoServiceImpl pedidoService;

    @Override
    @Transactional
    public RepartoDto crear(RequestRepartoDto dto) {
        Usuario repartidor = usuarioRepository.findById(dto.repartidorId)
                .orElseThrow(() -> new NotFoundException("No existe el usuario " + dto.repartidorId));
        if (repartidor.rol != ERol.REPARTIDOR) {
            throw new BusinessException("El usuario " + repartidor.nombre + " no es repartidor");
        }
        Zona zona = zonaRepository.findById(dto.zonaId)
                .orElseThrow(() -> new NotFoundException("No existe la zona " + dto.zonaId));
        Reparto reparto = new Reparto(null, repartidor, zona, dto.fecha, EEstadoReparto.ABIERTO);
        return repartoMapper.entityToDto(repartoRepository.save(reparto));
    }

    @Override
    public RepartoDto findById(Integer id) {
        return repartoMapper.entityToDto(buscar(id));
    }

    @Override
    @Transactional
    public PedidoDto asignarPedido(Integer repartoId, Integer pedidoId) {
        Reparto reparto = buscar(repartoId);
        if (reparto.estado != EEstadoReparto.ABIERTO) {
            throw new BusinessException("El reparto " + repartoId + " ya está cerrado");
        }
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("No existe el pedido " + pedidoId));
        if (pedido.estado != EEstadoPedido.PENDIENTE) {
            throw new BusinessException("Solo se pueden asignar pedidos pendientes");
        }
        if (!Objects.equals(pedido.zona.id, reparto.zona.id)) {
            throw new BusinessException("El pedido es de otra zona que el reparto");
        }
        pedido.reparto = reparto;
        pedido.estado = EEstadoPedido.ASIGNADO;
        return pedidoService.toDto(pedidoRepository.save(pedido));
    }

    @Override
    public List<PedidoDto> pedidosDelReparto(Integer repartoId) {
        buscar(repartoId);
        return pedidoRepository.findByRepartoId(repartoId).stream()
                .map(pedidoService::toDto)
                .toList();
    }

    private Reparto buscar(Integer id) {
        return repartoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el reparto " + id));
    }
}
