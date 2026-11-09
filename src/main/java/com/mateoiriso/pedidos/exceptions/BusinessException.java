package com.mateoiriso.pedidos.exceptions;

/** Se lanza cuando una operación viola una regla de negocio. Se responde como 409. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
