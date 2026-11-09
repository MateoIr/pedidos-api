package com.mateoiriso.pedidos.security;

import com.mateoiriso.pedidos.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final Duration expiracion;

    public JwtService(JwtEncoder encoder, @Value("${jwt.expiracion-horas:8}") long horas) {
        this.encoder = encoder;
        this.expiracion = Duration.ofHours(horas);
    }

    /** El subject es el ID del usuario: los controllers lo leen con authentication.getName(). */
    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("pedidos-api")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(expiracion))
                .subject(String.valueOf(usuario.id))
                .claim("email", usuario.email)
                .claim("rol", usuario.rol.name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long segundosDeValidez() {
        return expiracion.toSeconds();
    }
}
