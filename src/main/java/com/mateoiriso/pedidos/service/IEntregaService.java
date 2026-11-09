package com.mateoiriso.pedidos.service;

import com.mateoiriso.pedidos.model.dto.entrega.RequestEntregaDto;
import com.mateoiriso.pedidos.model.dto.entrega.RequestSobranteDto;
import com.mateoiriso.pedidos.model.dto.entrega.SobranteDto;
import com.mateoiriso.pedidos.model.dto.pedido.PedidoDto;

import java.util.List;

/** Operaciones del repartidor en la calle: entregas, cancelaciones y sobrantes. */
public interface IEntregaService {

    /** Registra cuánto se entregó de cada línea. Lo que no se entregó pasa a sobrantes del reparto. */
    PedidoDto registrarEntrega(Integer pedidoId, RequestEntregaDto dto);

    /** El cliente rechaza el pedido completo: todo pasa a sobrantes del reparto. */
    PedidoDto cancelar(Integer pedidoId);

    /**
     * Agrega unidades sobrantes a otro pedido del mismo reparto.
     * Si el producto ya estaba en el pedido, se respeta el precio original;
     * si no estaba, se cobra el precio vigente.
     */
    PedidoDto agregarSobrante(Integer pedidoId, RequestSobranteDto dto);

    List<SobranteDto> sobrantesDelReparto(Integer repartoId);
}
