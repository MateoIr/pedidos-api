package com.mateoiriso.pedidos.model.dto.entrega;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/** Una sugerencia ya validada: el pedido, el producto y el stock existen, y el precio sale de la regla. */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SugerenciaSobranteDto {
    public Integer pedidoId;
    public String cliente;
    public Integer productoId;
    public String producto;
    public Integer cantidad;
    /** Calculado por el sistema, nunca por la IA. */
    public BigDecimal precioUnitario;
    public Boolean respetaPrecioOriginal;
    public String motivo;
}
