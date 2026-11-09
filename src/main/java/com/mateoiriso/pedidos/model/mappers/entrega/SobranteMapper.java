package com.mateoiriso.pedidos.model.mappers.entrega;

import com.mateoiriso.pedidos.model.dto.entrega.SobranteDto;
import com.mateoiriso.pedidos.model.entity.Sobrante;
import com.mateoiriso.pedidos.model.mappers.producto.ProductoMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", implementationName = "SobranteMapperImpl", uses = {ProductoMapper.class})
public interface SobranteMapper {

    SobranteDto entityToDto(Sobrante sobrante);
}
