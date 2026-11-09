package com.mateoiriso.pedidos.model.dto.reparto;

import jakarta.validation.constraints.NotNull;
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
public class RequestRepartoDto {
    @NotNull(message = "El repartidor es obligatorio")
    public Integer repartidorId;

    @NotNull(message = "La zona es obligatoria")
    public Integer zonaId;

    @NotNull(message = "La fecha es obligatoria")
    public LocalDate fecha;
}
