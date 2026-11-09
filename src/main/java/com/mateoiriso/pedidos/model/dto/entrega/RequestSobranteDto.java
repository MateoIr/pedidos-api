package com.mateoiriso.pedidos.model.dto.entrega;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Agregar a un pedido unidades que sobraron en la camioneta. */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestSobranteDto {
    @NotNull(message = "El producto es obligatorio")
    public Integer productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    public Integer cantidad;
}
