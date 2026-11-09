package com.mateoiriso.pedidos.service.Impl;

import com.mateoiriso.pedidos.exceptions.NotFoundException;
import com.mateoiriso.pedidos.model.dto.producto.ProductoDto;
import com.mateoiriso.pedidos.model.dto.producto.RequestProductoDto;
import com.mateoiriso.pedidos.model.entity.Producto;
import com.mateoiriso.pedidos.model.mappers.producto.ProductoMapper;
import com.mateoiriso.pedidos.model.repository.ProductoRepository;
import com.mateoiriso.pedidos.service.IProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    @Override
    public List<ProductoDto> findActivos() {
        return productoRepository.findByActivoTrue().stream()
                .map(productoMapper::entityToDto)
                .toList();
    }

    @Override
    public ProductoDto findById(Integer id) {
        return productoMapper.entityToDto(buscar(id));
    }

    @Override
    @Transactional
    public ProductoDto save(RequestProductoDto dto) {
        return productoMapper.entityToDto(productoRepository.save(productoMapper.dtoToEntity(dto)));
    }

    @Override
    @Transactional
    public ProductoDto update(Integer id, RequestProductoDto dto) {
        Producto producto = buscar(id);
        producto.nombre = dto.nombre;
        producto.precio = dto.precio;
        return productoMapper.entityToDto(productoRepository.save(producto));
    }

    /** Baja lógica: un producto con pedidos asociados no se puede borrar. */
    @Override
    @Transactional
    public void desactivar(Integer id) {
        Producto producto = buscar(id);
        producto.activo = false;
        productoRepository.save(producto);
    }

    private Producto buscar(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el producto " + id));
    }
}
