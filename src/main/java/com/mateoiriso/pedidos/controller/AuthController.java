package com.mateoiriso.pedidos.controller;

import com.mateoiriso.pedidos.model.dto.auth.RequestLoginDto;
import com.mateoiriso.pedidos.model.dto.auth.RequestRegistroDto;
import com.mateoiriso.pedidos.model.dto.auth.TokenDto;
import com.mateoiriso.pedidos.model.dto.usuario.UsuarioDto;
import com.mateoiriso.pedidos.service.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUsuarioService usuarioService;

    @Operation(summary = "Registrarse como cliente")
    @PostMapping("registro")
    ResponseEntity<UsuarioDto> registro(@Valid @RequestBody RequestRegistroDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrarCliente(dto));
    }

    @Operation(summary = "Iniciar sesión y obtener el token JWT")
    @PostMapping("login")
    ResponseEntity<TokenDto> login(@Valid @RequestBody RequestLoginDto dto) {
        return ResponseEntity.ok(usuarioService.login(dto));
    }
}
