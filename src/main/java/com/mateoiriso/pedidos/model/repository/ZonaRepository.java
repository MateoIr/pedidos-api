package com.mateoiriso.pedidos.model.repository;

import com.mateoiriso.pedidos.model.entity.Zona;
import org.springframework.data.repository.CrudRepository;

public interface ZonaRepository extends CrudRepository<Zona, Integer> {
    boolean existsByNombreIgnoreCase(String nombre);
}
