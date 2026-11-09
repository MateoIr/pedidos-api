package com.mateoiriso.pedidos.exceptions;

import java.time.LocalDateTime;
import java.util.List;

/** Formato único de error que devuelve la API. */
public record ErrorDto(int status, String mensaje, List<String> detalles, LocalDateTime fecha) {
    public static ErrorDto of(int status, String mensaje, List<String> detalles) {
        return new ErrorDto(status, mensaje, detalles, LocalDateTime.now());
    }
}
