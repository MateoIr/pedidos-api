package com.mateoiriso.pedidos.model.mappers.pedido;

import com.mateoiriso.pedidos.model.dto.pedido.PedidoDto;
import com.mateoiriso.pedidos.model.entity.Pedido;
import com.mateoiriso.pedidos.model.mappers.usuario.UsuarioMapper;
import com.mateoiriso.pedidos.model.mappers.zona.ZonaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Los detalles y el total los completa el servicio: Pedido no tiene la colección
 * de detalles mapeada (se consultan desde PedidoDetalleRepository).
 */
@Mapper(componentModel = "spring", implementationName = "PedidoMapperImpl", uses = {UsuarioMapper.class, ZonaMapper.class})
public interface PedidoMapper {

    @Mapping(target = "repartoId", source = "reparto.id")
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "total", ignore = true)
    PedidoDto entityToDto(Pedido pedido);
}
