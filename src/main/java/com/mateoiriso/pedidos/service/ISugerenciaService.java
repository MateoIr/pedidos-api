package com.mateoiriso.pedidos.service;

import com.mateoiriso.pedidos.model.dto.entrega.SugerenciasDto;

public interface ISugerenciaService {

    /** Pide a la IA qué sobrantes ofrecer a qué pedidos del reparto y devuelve solo las sugerencias válidas. */
    SugerenciasDto sugerirSobrantes(Integer repartoId);
}
