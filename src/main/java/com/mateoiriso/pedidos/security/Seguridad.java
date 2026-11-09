package com.mateoiriso.pedidos.security;

import com.mateoiriso.pedidos.model.repository.PedidoRepository;
import com.mateoiriso.pedidos.model.repository.RepartoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Chequeos de propiedad que se usan desde @PreAuthorize, por ejemplo:
 * {@code @PreAuthorize("hasRole('ADMIN') or @seguridad.esDuenioDelPedido(#id, authentication)")}.
 * Así un cliente solo ve sus pedidos y un repartidor solo opera los de su reparto.
 */
@Component("seguridad")
@RequiredArgsConstructor
public class Seguridad {

    private final PedidoRepository pedidoRepository;
    private final RepartoRepository repartoRepository;

    public static Integer usuarioId(Authentication authentication) {
        return Integer.valueOf(authentication.getName());
    }

    public boolean esDuenioDelPedido(Integer pedidoId, Authentication authentication) {
        return pedidoRepository.findById(pedidoId)
                .map(p -> Objects.equals(p.cliente.id, usuarioId(authentication)))
                .orElse(false);
    }

    public boolean esRepartidorDelPedido(Integer pedidoId, Authentication authentication) {
        return pedidoRepository.findById(pedidoId)
                .map(p -> p.reparto != null && Objects.equals(p.reparto.repartidor.id, usuarioId(authentication)))
                .orElse(false);
    }

    public boolean esRepartidorDelReparto(Integer repartoId, Authentication authentication) {
        return repartoRepository.findById(repartoId)
                .map(r -> Objects.equals(r.repartidor.id, usuarioId(authentication)))
                .orElse(false);
    }
}
