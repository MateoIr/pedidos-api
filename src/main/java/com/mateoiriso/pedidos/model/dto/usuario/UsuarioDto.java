package com.mateoiriso.pedidos.model.dto.usuario;

import com.mateoiriso.pedidos.model.enums.ERol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Nunca incluye la contraseña, ni siquiera hasheada. */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioDto {
    public Integer id;
    public String nombre;
    public String email;
    public ERol rol;
}
