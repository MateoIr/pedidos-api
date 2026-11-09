package com.mateoiriso.pedidos.model.dto.reparto;

import com.mateoiriso.pedidos.model.dto.usuario.UsuarioDto;
import com.mateoiriso.pedidos.model.dto.zona.ZonaDto;
import com.mateoiriso.pedidos.model.enums.EEstadoReparto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RepartoDto {
    public Integer id;
    public UsuarioDto repartidor;
    public ZonaDto zona;
    public LocalDate fecha;
    public EEstadoReparto estado;
}
