package com.mateoiriso.pedidos.model.mappers.zona;

import com.mateoiriso.pedidos.model.dto.zona.RequestZonaDto;
import com.mateoiriso.pedidos.model.dto.zona.ZonaDto;
import com.mateoiriso.pedidos.model.entity.Zona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", implementationName = "ZonaMapperImpl")
public interface ZonaMapper {

    ZonaDto entityToDto(Zona zona);

    @Mapping(target = "id", ignore = true)
    Zona dtoToEntity(RequestZonaDto dto);
}
