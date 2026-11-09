package com.mateoiriso.pedidos.model.mappers.reparto;

import com.mateoiriso.pedidos.model.dto.reparto.RepartoDto;
import com.mateoiriso.pedidos.model.entity.Reparto;
import com.mateoiriso.pedidos.model.mappers.usuario.UsuarioMapper;
import com.mateoiriso.pedidos.model.mappers.zona.ZonaMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", implementationName = "RepartoMapperImpl", uses = {UsuarioMapper.class, ZonaMapper.class})
public interface RepartoMapper {

    RepartoDto entityToDto(Reparto reparto);
}
