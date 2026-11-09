package com.mateoiriso.pedidos.service.ia;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * Formato JSON que Claude tiene que devolver (structured output).
 * El SDK genera el JSON Schema a partir de estos records, así que la respuesta llega ya tipada.
 */
public record RespuestaSugerenciasIa(
        @JsonPropertyDescription("Sugerencias de qué sobrante ofrecer a qué pedido. Puede ser una lista vacía.")
        List<SugerenciaIa> sugerencias) {

    public record SugerenciaIa(
            @JsonPropertyDescription("ID del pedido al que se le ofrece el producto")
            int pedidoId,
            @JsonPropertyDescription("ID del producto sobrante que se ofrece")
            int productoId,
            @JsonPropertyDescription("Cantidad de unidades a ofrecer")
            int cantidad,
            @JsonPropertyDescription("Motivo breve en español, para mostrarle al repartidor")
            String motivo) {
    }
}
