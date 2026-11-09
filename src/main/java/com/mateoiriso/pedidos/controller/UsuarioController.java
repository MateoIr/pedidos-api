package com.mateoiriso.pedidos.controller;

import com.mateoiriso.pedidos.model.dto.auth.RequestUsuarioDto;
import com.mateoiriso.pedidos.model.dto.usuario.UsuarioDto;
import com.mateoiriso.pedidos.security.Seguridad;
import com.mateoiriso.pedidos.service.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuarios")
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;

    @Operation(summary = "Crear un usuario con cualquier rol (por ejemplo, un repartidor)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    ResponseEntity<UsuarioDto> crear(@Valid @RequestBody RequestUsuarioDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(dto));
    }

    @Operation(summary = "Datos del usuario logueado")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("me")
    ResponseEntity<UsuarioDto> me(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.findById(Seguridad.usuarioId(authentication)));
    }
}
