package com.mateoiriso.pedidos.service.ia;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.mateoiriso.pedidos.exceptions.IaNoDisponibleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Llama a Claude con el SDK oficial de Anthropic y pide la respuesta en un JSON con formato fijo.
 * Solo se crea si app.ia.habilitada=true (la API key se lee de ANTHROPIC_API_KEY).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ia.habilitada", havingValue = "true")
public class ClaudeSugerenciaClient implements ISugerenciaIaClient {

    static final String INSTRUCCIONES = """
            Sos el asistente de un repartidor de una distribuidora. Durante el reparto quedaron productos
            sobrantes en la camioneta (por entregas parciales o pedidos cancelados) y hay que ofrecérselos
            a otros clientes del mismo reparto que todavía no recibieron su pedido.

            Sugerí qué sobrante ofrecerle a qué pedido. Criterios, en orden:
            1. Priorizá los pedidos que ya incluyen ese producto: es más probable que lo acepten
               y además se les respeta el precio original.
            2. Repartí las unidades entre varios pedidos antes que darle todo a uno solo.
            3. No sugieras más unidades de las que sobran, ni pedidos o productos que no estén en la lista.
            Si no hay una sugerencia razonable, devolvé la lista vacía. El motivo va en español y en una línea.
            """;

    private final AnthropicClient client;
    private final String modelo;

    public ClaudeSugerenciaClient(@Value("${app.ia.modelo:claude-opus-5-5}") String modelo) {
        this.client = AnthropicOkHttpClient.fromEnv();
        this.modelo = modelo;
    }

    @Override
    public RespuestaSugerenciasIa sugerir(String estadoDelReparto) {
        StructuredMessageCreateParams<RespuestaSugerenciasIa> params = MessageCreateParams.builder()
                .model(modelo)
                .maxTokens(16000L)
                .system(INSTRUCCIONES)
                .outputConfig(RespuestaSugerenciasIa.class)
                .addUserMessage(estadoDelReparto)
                .build();
        try {
            StructuredMessage<RespuestaSugerenciasIa> respuesta = client.messages().create(params);
            if (respuesta.stopReason().map(StopReason.REFUSAL::equals).orElse(false)) {
                throw new IaNoDisponibleException("El modelo no respondió esta consulta");
            }
            return respuesta.content().stream()
                    .flatMap(bloque -> bloque.text().stream())
                    .map(texto -> texto.text())
                    .findFirst()
                    .orElseThrow(() -> new IaNoDisponibleException("El modelo no devolvió sugerencias"));
        } catch (AnthropicServiceException e) {
            log.warn("Error llamando a Claude: {}", e.getMessage());
            throw new IaNoDisponibleException("El servicio de IA no está disponible en este momento");
        }
    }

    @Override
    public String modelo() {
        return modelo;
    }
}
