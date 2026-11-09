package com.mateoiriso.pedidos.model.dto.auth;

import com.mateoiriso.pedidos.model.dto.usuario.UsuarioDto;

public record TokenDto(String token, String tipo, long expiraEnSegundos, UsuarioDto usuario) {
}
