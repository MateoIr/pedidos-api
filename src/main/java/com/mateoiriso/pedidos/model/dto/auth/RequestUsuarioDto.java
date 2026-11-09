package com.mateoiriso.pedidos.model.dto.auth;

import com.mateoiriso.pedidos.model.enums.ERol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Alta hecha por un ADMIN: puede crear repartidores u otros administradores. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestUsuarioDto {
    @NotBlank(message = "El nombre es obligatorio")
    public String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    public String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    public String password;

    @NotNull(message = "El rol es obligatorio")
    public ERol rol;
}
