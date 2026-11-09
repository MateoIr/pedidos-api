package com.mateoiriso.pedidos.controller;

import com.mateoiriso.pedidos.model.dto.entrega.RequestEntregaDto;
import com.mateoiriso.pedidos.model.dto.entrega.RequestSobranteDto;
import com.mateoiriso.pedidos.model.dto.pedido.PedidoDto;
import com.mateoiriso.pedidos.model.dto.pedido.RequestPedidoDto;
import com.mateoiriso.pedidos.security.Seguridad;
import com.mateoiriso.pedidos.service.IEntregaService;
import com.mateoiriso.pedidos.service.IPedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Pedidos")
@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final IPedidoService pedidoService;
    private final IEntregaService entregaService;

    @Operation(summary = "Crear un pedido (el cliente se toma del token, no del body)")
    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping
    ResponseEntity<PedidoDto> crear(@Valid @RequestBody RequestPedidoDto dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.crear(Seguridad.usuarioId(authentication), dto));
    }

    @Operation(summary = "Mis pedidos")
    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("mios")
    ResponseEntity<List<PedidoDto>> mios(Authentication authentication) {
        return ResponseEntity.ok(pedidoService.findByCliente(Seguridad.usuarioId(authentication)));
    }

    @Operation(summary = "Ver un pedido: admin, el cliente dueño o el repartidor asignado")
    @PreAuthorize("hasRole('ADMIN') or @seguridad.esDuenioDelPedido(#id, authentication)"
            + " or @seguridad.esRepartidorDelPedido(#id, authentication)")
    @GetMapping("{id}")
    ResponseEntity<PedidoDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.findById(id));
    }

    @Operation(summary = "Registrar lo que se entregó de cada línea; lo demás pasa a sobrantes")
    @PreAuthorize("hasRole('ADMIN') or @seguridad.esRepartidorDelPedido(#id, authentication)")
    @PostMapping("{id}/entrega")
    ResponseEntity<PedidoDto> registrarEntrega(@PathVariable Integer id, @Valid @RequestBody RequestEntregaDto dto) {
        return ResponseEntity.ok(entregaService.registrarEntrega(id, dto));
    }

    @Operation(summary = "El cliente rechaza el pedido completo")
    @PreAuthorize("hasRole('ADMIN') or @seguridad.esRepartidorDelPedido(#id, authentication)")
    @PostMapping("{id}/cancelacion")
    ResponseEntity<PedidoDto> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(entregaService.cancelar(id));
    }

    @Operation(summary = "Agregar al pedido productos sobrantes del reparto (con la regla de precios)")
    @PreAuthorize("hasRole('ADMIN') or @seguridad.esRepartidorDelPedido(#id, authentication)")
    @PostMapping("{id}/sobrantes")
    ResponseEntity<PedidoDto> agregarSobrante(@PathVariable Integer id, @Valid @RequestBody RequestSobranteDto dto) {
        return ResponseEntity.ok(entregaService.agregarSobrante(id, dto));
    }
}
