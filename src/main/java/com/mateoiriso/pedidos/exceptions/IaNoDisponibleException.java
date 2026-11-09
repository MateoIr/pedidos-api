package com.mateoiriso.pedidos.exceptions;

/** La IA está deshabilitada o falló. Se responde como 503: el resto de la API sigue funcionando. */
public class IaNoDisponibleException extends RuntimeException {
    public IaNoDisponibleException(String message) {
        super(message);
    }
}
