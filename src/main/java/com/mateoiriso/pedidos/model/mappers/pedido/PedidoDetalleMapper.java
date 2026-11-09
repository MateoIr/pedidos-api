package com.mateoiriso.pedidos.model.mappers.pedido;

import com.mateoiriso.pedidos.model.dto.pedido.PedidoDetalleDto;
import com.mateoiriso.pedidos.model.entity.PedidoDetalle;
import com.mateoiriso.pedidos.model.mappers.producto.ProductoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", implementationName = "PedidoDetalleMapperImpl", uses = {ProductoMapper.class})
public interface PedidoDetalleMapper {

    @Mapping(target = "subtotal", expression = "java(subtotal(detalle))")
    PedidoDetalleDto entityToDto(PedidoDetalle detalle);

    /** Si ya se entregó, se cobra lo entregado; si no, lo pedido. */
    default BigDecimal subtotal(PedidoDetalle detalle) {
        int unidades = detalle.cantidadEntregada != null ? detalle.cantidadEntregada : detalle.cantidad;
        return detalle.precioUnitario.multiply(BigDecimal.valueOf(unidades));
    }
}
