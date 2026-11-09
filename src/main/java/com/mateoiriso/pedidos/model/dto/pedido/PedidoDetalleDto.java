package com.mateoiriso.pedidos.model.dto.pedido;

import com.mateoiriso.pedidos.model.dto.producto.ProductoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PedidoDetalleDto {
    public Integer id;
    public ProductoDto producto;
    public Integer cantidad;
    public BigDecimal precioUnitario;
    public Integer cantidadEntregada;
    public Boolean agregadoDeSobrante;
    public BigDecimal subtotal;
}
