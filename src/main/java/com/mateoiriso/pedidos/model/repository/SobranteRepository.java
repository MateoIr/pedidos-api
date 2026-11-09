package com.mateoiriso.pedidos.model.repository;

import com.mateoiriso.pedidos.model.entity.Sobrante;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SobranteRepository extends CrudRepository<Sobrante, Integer> {
    List<Sobrante> findByRepartoId(Integer repartoId);

    Optional<Sobrante> findByRepartoIdAndProductoId(Integer repartoId, Integer productoId);
}
