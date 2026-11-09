package com.mateoiriso.pedidos.service;

import com.mateoiriso.pedidos.model.dto.pedido.PedidoDto;
import com.mateoiriso.pedidos.model.dto.pedido.RequestPedidoDto;

import java.util.List;

public interface IPedidoService {
    PedidoDto crear(Integer clienteId, RequestPedidoDto dto);

    PedidoDto findById(Integer id);

    List<PedidoDto> findByCliente(Integer clienteId);
}
