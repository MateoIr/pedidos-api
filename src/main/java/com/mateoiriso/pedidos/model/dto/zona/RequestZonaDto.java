package com.mateoiriso.pedidos.model.dto.zona;

import jakarta.validation.constraints.NotBlank;
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
public class RequestZonaDto {
    @NotBlank(message = "El nombre de la zona es obligatorio")
    public String nombre;
}
