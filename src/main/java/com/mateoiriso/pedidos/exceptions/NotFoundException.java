package com.mateoiriso.pedidos.exceptions;

/** Se lanza cuando se busca algo por ID y no existe. Se responde como 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
