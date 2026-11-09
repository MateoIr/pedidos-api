package com.mateoiriso.pedidos.service;

import com.mateoiriso.pedidos.model.dto.producto.ProductoDto;
import com.mateoiriso.pedidos.model.dto.producto.RequestProductoDto;

import java.util.List;

public interface IProductoService {
    List<ProductoDto> findActivos();

    ProductoDto findById(Integer id);

    ProductoDto save(RequestProductoDto dto);

    /** Cambia nombre y precio vigente. Los pedidos ya creados conservan su precio. */
    ProductoDto update(Integer id, RequestProductoDto dto);

    void desactivar(Integer id);
}
