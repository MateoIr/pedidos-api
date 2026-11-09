package com.mateoiriso.pedidos.model.dto.entrega;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/** Resultado de la entrega: hay que informar todas las líneas del pedido. */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestEntregaDto {
    @NotEmpty(message = "Hay que informar las cantidades entregadas")
    public List<@Valid RequestEntregaItemDto> items;
}
