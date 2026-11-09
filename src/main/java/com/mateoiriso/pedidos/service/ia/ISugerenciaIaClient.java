package com.mateoiriso.pedidos.service.ia;

/**
 * Abstracción del modelo de IA. El servicio depende de esta interfaz y no del SDK,
 * así se puede testear con un mock y cambiar de proveedor sin tocar la lógica de negocio.
 */
public interface ISugerenciaIaClient {

    /** Recibe el estado del reparto en texto y devuelve sugerencias sin validar. */
    RespuestaSugerenciasIa sugerir(String estadoDelReparto);

    /** Nombre del modelo, para informarlo en la respuesta. */
    String modelo();
}
