package com.mateoiriso.pedidos.service.Impl;

import com.mateoiriso.pedidos.exceptions.BusinessException;
import com.mateoiriso.pedidos.exceptions.NotFoundException;
import com.mateoiriso.pedidos.model.dto.zona.RequestZonaDto;
import com.mateoiriso.pedidos.model.dto.zona.ZonaDto;
import com.mateoiriso.pedidos.model.mappers.zona.ZonaMapper;
import com.mateoiriso.pedidos.model.repository.ZonaRepository;
import com.mateoiriso.pedidos.service.IZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ZonaServiceImpl implements IZonaService {

    private final ZonaRepository zonaRepository;
    private final ZonaMapper zonaMapper;

    @Override
    public List<ZonaDto> findAll() {
        return StreamSupport.stream(zonaRepository.findAll().spliterator(), false)
                .map(zonaMapper::entityToDto)
                .toList();
    }

    @Override
    public ZonaDto findById(Integer id) {
        return zonaRepository.findById(id)
                .map(zonaMapper::entityToDto)
                .orElseThrow(() -> new NotFoundException("No existe la zona " + id));
    }

    @Override
    @Transactional
    public ZonaDto save(RequestZonaDto dto) {
        if (zonaRepository.existsByNombreIgnoreCase(dto.nombre)) {
            throw new BusinessException("Ya existe una zona llamada " + dto.nombre);
        }
        return zonaMapper.entityToDto(zonaRepository.save(zonaMapper.dtoToEntity(dto)));
    }
}
