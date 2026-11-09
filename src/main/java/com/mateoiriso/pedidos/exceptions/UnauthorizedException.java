package com.mateoiriso.pedidos.exceptions;

/** Credenciales inválidas en el login. Se responde como 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
