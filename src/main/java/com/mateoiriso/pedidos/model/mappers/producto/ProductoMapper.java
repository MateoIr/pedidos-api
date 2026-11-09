package com.mateoiriso.pedidos.model.mappers.producto;

import com.mateoiriso.pedidos.model.dto.producto.ProductoDto;
import com.mateoiriso.pedidos.model.dto.producto.RequestProductoDto;
import com.mateoiriso.pedidos.model.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", implementationName = "ProductoMapperImpl")
public interface ProductoMapper {

    ProductoDto entityToDto(Producto producto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", constant = "true")
    Producto dtoToEntity(RequestProductoDto dto);
}
