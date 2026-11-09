package com.mateoiriso.pedidos.model.dto.entrega;

import com.mateoiriso.pedidos.model.dto.producto.ProductoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SobranteDto {
    public Integer id;
    public ProductoDto producto;
    public Integer cantidad;
}
