package com.mateoiriso.pedidos.model.dto.entrega;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Cuántas unidades de una línea del pedido se entregaron realmente. */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestEntregaItemDto {
    @NotNull(message = "La línea del pedido es obligatoria")
    public Integer detalleId;

    @NotNull(message = "La cantidad entregada es obligatoria")
    @Min(value = 0, message = "La cantidad entregada no puede ser negativa")
    public Integer cantidadEntregada;
}
