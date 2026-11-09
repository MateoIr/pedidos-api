package com.mateoiriso.pedidos.controller;

import com.mateoiriso.pedidos.model.dto.producto.ProductoDto;
import com.mateoiriso.pedidos.model.dto.producto.RequestProductoDto;
import com.mateoiriso.pedidos.service.IProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Productos")
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final IProductoService productoService;

    @Operation(summary = "Catálogo de productos activos")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    ResponseEntity<List<ProductoDto>> findActivos() {
        return ResponseEntity.ok(productoService.findActivos());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("{id}")
    ResponseEntity<ProductoDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    ResponseEntity<ProductoDto> save(@Valid @RequestBody RequestProductoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.save(dto));
    }

    @Operation(summary = "Actualizar nombre y precio vigente (no afecta pedidos ya creados)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("{id}")
    ResponseEntity<ProductoDto> update(@PathVariable Integer id, @Valid @RequestBody RequestProductoDto dto) {
        return ResponseEntity.ok(productoService.update(id, dto));
    }

    @Operation(summary = "Baja lógica del producto")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        productoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
