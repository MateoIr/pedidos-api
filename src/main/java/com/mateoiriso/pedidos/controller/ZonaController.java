package com.mateoiriso.pedidos.controller;

import com.mateoiriso.pedidos.model.dto.zona.RequestZonaDto;
import com.mateoiriso.pedidos.model.dto.zona.ZonaDto;
import com.mateoiriso.pedidos.service.IZonaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Zonas")
@RestController
@RequestMapping("/api/v1/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final IZonaService zonaService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    ResponseEntity<List<ZonaDto>> findAll() {
        return ResponseEntity.ok(zonaService.findAll());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("{id}")
    ResponseEntity<ZonaDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(zonaService.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    ResponseEntity<ZonaDto> save(@Valid @RequestBody RequestZonaDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(zonaService.save(dto));
    }
}
