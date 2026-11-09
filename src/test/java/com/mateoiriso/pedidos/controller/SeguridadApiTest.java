package com.mateoiriso.pedidos.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba la API por HTTP: autenticación con JWT y permisos por rol y por dueño del recurso. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadApiTest {

    @Autowired MockMvc mvc;

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginConCredencialesInvalidasDevuelve401() throws Exception {
        postJson("/api/v1/auth/login", null, """
                {"email": "admin@test.com", "password": "incorrecta"}""")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Email o contraseña incorrectos"));
    }

    @Test
    void registroInvalidoDevuelve400ConLosCamposConError() throws Exception {
        postJson("/api/v1/auth/registro", null, """
                {"nombre": "", "email": "no-es-un-email", "password": "corta"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.length()").value(3));
    }

    @Test
    void unClienteNoPuedeCrearProductos() throws Exception {
        String cliente = registrarYLoguear("Cliente", "cliente1@test.com");
        postJson("/api/v1/productos", cliente, """
                {"nombre": "Agua", "precio": 1000}""")
                .andExpect(status().isForbidden());
    }

    @Test
    void cadaUnoSoloAccedeASusPedidosYRepartos() throws Exception {
        String admin = login("admin@test.com", "admin-de-prueba");
        Integer zona = id(postJson("/api/v1/zonas", admin, """
                {"nombre": "Norte"}""").andExpect(status().isCreated()));
        Integer agua = id(postJson("/api/v1/productos", admin, """
                {"nombre": "Agua 2L", "precio": 1000}""").andExpect(status().isCreated()));
        Integer repartidor = id(postJson("/api/v1/usuarios", admin, """
                {"nombre": "Repartidor", "email": "rep1@test.com", "password": "clave-repartidor", "rol": "REPARTIDOR"}"""));
        postJson("/api/v1/usuarios", admin, """
                {"nombre": "Otro", "email": "rep2@test.com", "password": "clave-repartidor", "rol": "REPARTIDOR"}""");

        String ana = registrarYLoguear("Ana", "ana.api@test.com");
        String bruno = registrarYLoguear("Bruno", "bruno.api@test.com");

        ResultActions creado = postJson("/api/v1/pedidos", ana, """
                {"zonaId": %d, "items": [{"productoId": %d, "cantidad": 2}]}""".formatted(zona, agua))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente.email").value("ana.api@test.com"))
                .andExpect(jsonPath("$.total").value(2000.0));
        Integer pedido = id(creado);
        Integer detalle = JsonPath.read(creado.andReturn().getResponse().getContentAsString(), "$.detalles[0].id");

        // Ana ve su pedido; Bruno no.
        mvc.perform(get("/api/v1/pedidos/" + pedido).header("Authorization", bearer(ana))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/pedidos/" + pedido).header("Authorization", bearer(bruno))).andExpect(status().isForbidden());

        Integer reparto = id(postJson("/api/v1/repartos", admin, """
                {"repartidorId": %d, "zonaId": %d, "fecha": "2026-11-09"}""".formatted(repartidor, zona)));
        postJson("/api/v1/repartos/" + reparto + "/pedidos/" + pedido, admin, "").andExpect(status().isOk());

        String entrega = """
                {"items": [{"detalleId": %d, "cantidadEntregada": 2}]}""".formatted(detalle);
        // Un repartidor que no tiene ese pedido no puede registrar la entrega.
        postJson("/api/v1/pedidos/" + pedido + "/entrega", login("rep2@test.com", "clave-repartidor"), entrega)
                .andExpect(status().isForbidden());
        // El repartidor asignado sí.
        postJson("/api/v1/pedidos/" + pedido + "/entrega", login("rep1@test.com", "clave-repartidor"), entrega)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));
    }

    private ResultActions postJson(String url, String token, String body) throws Exception {
        var request = post(url).contentType(MediaType.APPLICATION_JSON).content(body);
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        return mvc.perform(request);
    }

    private String login(String email, String password) throws Exception {
        String json = postJson("/api/v1/auth/login", null, """
                {"email": "%s", "password": "%s"}""".formatted(email, password))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    private String registrarYLoguear(String nombre, String email) throws Exception {
        postJson("/api/v1/auth/registro", null, """
                {"nombre": "%s", "email": "%s", "password": "clave-cliente"}""".formatted(nombre, email))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist());
        return login(email, "clave-cliente");
    }

    private Integer id(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
