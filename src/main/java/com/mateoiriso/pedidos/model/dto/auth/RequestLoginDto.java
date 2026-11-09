package com.mateoiriso.pedidos.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestLoginDto {
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    public String email;

    @NotBlank(message = "La contraseña es obligatoria")
    public String password;
}
