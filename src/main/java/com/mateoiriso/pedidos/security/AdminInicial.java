package com.mateoiriso.pedidos.security;

import com.mateoiriso.pedidos.model.entity.Usuario;
import com.mateoiriso.pedidos.model.enums.ERol;
import com.mateoiriso.pedidos.model.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el primer administrador al arrancar, a partir de variables de entorno.
 * Así no queda ninguna contraseña escrita en el código ni en las migraciones.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInicial implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String email;

    @Value("${app.admin.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        String emailNormalizado = email.trim().toLowerCase();
        if (emailNormalizado.isBlank() || password.isBlank() || usuarioRepository.existsByEmail(emailNormalizado)) {
            return;
        }
        usuarioRepository.save(new Usuario(null, "Administrador", emailNormalizado, passwordEncoder.encode(password), ERol.ADMIN));
        log.info("Administrador inicial creado: {}", emailNormalizado);
    }
}
