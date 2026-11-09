package com.mateoiriso.pedidos.service.Impl;

import com.mateoiriso.pedidos.exceptions.BusinessException;
import com.mateoiriso.pedidos.exceptions.NotFoundException;
import com.mateoiriso.pedidos.exceptions.UnauthorizedException;
import com.mateoiriso.pedidos.model.dto.auth.RequestLoginDto;
import com.mateoiriso.pedidos.model.dto.auth.RequestRegistroDto;
import com.mateoiriso.pedidos.model.dto.auth.RequestUsuarioDto;
import com.mateoiriso.pedidos.model.dto.auth.TokenDto;
import com.mateoiriso.pedidos.model.dto.usuario.UsuarioDto;
import com.mateoiriso.pedidos.model.entity.Usuario;
import com.mateoiriso.pedidos.model.enums.ERol;
import com.mateoiriso.pedidos.model.mappers.usuario.UsuarioMapper;
import com.mateoiriso.pedidos.model.repository.UsuarioRepository;
import com.mateoiriso.pedidos.security.JwtService;
import com.mateoiriso.pedidos.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UsuarioDto registrarCliente(RequestRegistroDto dto) {
        return guardar(dto.nombre, dto.email, dto.password, ERol.CLIENTE);
    }

    @Override
    @Transactional
    public UsuarioDto crear(RequestUsuarioDto dto) {
        return guardar(dto.nombre, dto.email, dto.password, dto.rol);
    }

    @Override
    public TokenDto login(RequestLoginDto dto) {
        String email = dto.email.trim().toLowerCase();
        // Mismo mensaje si el email no existe o la contraseña es incorrecta: no revelamos qué emails están registrados.
        Usuario usuario = usuarioRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(dto.password, u.passwordHash))
                .orElseThrow(() -> new UnauthorizedException("Email o contraseña incorrectos"));
        return new TokenDto(jwtService.generarToken(usuario), "Bearer", jwtService.segundosDeValidez(),
                usuarioMapper.entityToDto(usuario));
    }

    @Override
    public UsuarioDto findById(Integer id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::entityToDto)
                .orElseThrow(() -> new NotFoundException("No existe el usuario " + id));
    }

    private UsuarioDto guardar(String nombre, String email, String password, ERol rol) {
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new BusinessException("Ya existe un usuario con ese email");
        }
        Usuario usuario = new Usuario(null, nombre, emailNormalizado, passwordEncoder.encode(password), rol);
        return usuarioMapper.entityToDto(usuarioRepository.save(usuario));
    }
}
