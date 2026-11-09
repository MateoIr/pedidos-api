package com.mateoiriso.pedidos.model.repository;

import com.mateoiriso.pedidos.model.entity.Pedido;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PedidoRepository extends CrudRepository<Pedido, Integer> {
    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(Integer clienteId);

    List<Pedido> findByRepartoId(Integer repartoId);
}
