package com.mateoiriso.pedidos.model.dto.entrega;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Respuesta del endpoint de sugerencias. Las sugerencias no se aplican solas:
 * el repartidor decide y usa POST /pedidos/{id}/sobrantes para agregarlas.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SugerenciasDto {
    public Integer repartoId;
    public String modelo;
    public List<SugerenciaSobranteDto> sugerencias;
    /** Sugerencias de la IA que no pasaron la validación, con el motivo. */
    public List<String> descartadas;
}
