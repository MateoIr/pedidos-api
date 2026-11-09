package com.mateoiriso.pedidos.model.dto.producto;

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
public class ProductoDto {
    public Integer id;
    public String nombre;
    public BigDecimal precio;
    public Boolean activo;
}
