package com.mateoiriso.pedidos.model.repository;

import com.mateoiriso.pedidos.model.entity.Producto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductoRepository extends CrudRepository<Producto, Integer> {
    List<Producto> findByActivoTrue();
}
