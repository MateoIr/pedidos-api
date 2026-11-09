package com.mateoiriso.pedidos.service;

import com.mateoiriso.pedidos.model.dto.zona.RequestZonaDto;
import com.mateoiriso.pedidos.model.dto.zona.ZonaDto;

import java.util.List;

public interface IZonaService {
    List<ZonaDto> findAll();

    ZonaDto findById(Integer id);

    ZonaDto save(RequestZonaDto dto);
}
