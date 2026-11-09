package com.mateoiriso.pedidos.service;

import com.mateoiriso.pedidos.model.dto.auth.RequestLoginDto;
import com.mateoiriso.pedidos.model.dto.auth.RequestRegistroDto;
import com.mateoiriso.pedidos.model.dto.auth.RequestUsuarioDto;
import com.mateoiriso.pedidos.model.dto.auth.TokenDto;
import com.mateoiriso.pedidos.model.dto.usuario.UsuarioDto;

public interface IUsuarioService {
    UsuarioDto registrarCliente(RequestRegistroDto dto);

    UsuarioDto crear(RequestUsuarioDto dto);

    TokenDto login(RequestLoginDto dto);

    UsuarioDto findById(Integer id);
}
