package com.mateoiriso.pedidos.service;

import com.mateoiriso.pedidos.model.dto.pedido.PedidoDto;
import com.mateoiriso.pedidos.model.dto.reparto.RepartoDto;
import com.mateoiriso.pedidos.model.dto.reparto.RequestRepartoDto;

import java.util.List;

public interface IRepartoService {
    RepartoDto crear(RequestRepartoDto dto);

    RepartoDto findById(Integer id);

    /** Asigna un pedido pendiente de la misma zona a un reparto abierto. */
    PedidoDto asignarPedido(Integer repartoId, Integer pedidoId);

    List<PedidoDto> pedidosDelReparto(Integer repartoId);
}
