package com.mateoiriso.pedidos.model.repository;

import com.mateoiriso.pedidos.model.entity.PedidoDetalle;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoDetalleRepository extends CrudRepository<PedidoDetalle, Integer> {
    List<PedidoDetalle> findByPedidoId(Integer pedidoId);

    Optional<PedidoDetalle> findByPedidoIdAndProductoId(Integer pedidoId, Integer productoId);
}
