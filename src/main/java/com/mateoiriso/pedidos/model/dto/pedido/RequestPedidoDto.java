package com.mateoiriso.pedidos.model.dto.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/** El cliente no viene en el body: se toma del token JWT. */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPedidoDto {
    @NotNull(message = "La zona es obligatoria")
    public Integer zonaId;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    public List<@Valid RequestItemDto> items;
}
