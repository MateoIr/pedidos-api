package com.mateoiriso.pedidos.model.mappers.usuario;

import com.mateoiriso.pedidos.model.dto.usuario.UsuarioDto;
import com.mateoiriso.pedidos.model.entity.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", implementationName = "UsuarioMapperImpl")
public interface UsuarioMapper {

    /** passwordHash no existe en UsuarioDto, así que nunca sale por la API. */
    UsuarioDto entityToDto(Usuario usuario);
}
