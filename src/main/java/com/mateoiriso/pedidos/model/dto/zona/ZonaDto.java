package com.mateoiriso.pedidos.model.dto.zona;

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
public class ZonaDto {
    public Integer id;
    public String nombre;
}
