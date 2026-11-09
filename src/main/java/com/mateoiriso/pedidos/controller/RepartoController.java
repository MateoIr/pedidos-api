package com.mateoiriso.pedidos.controller;

import com.mateoiriso.pedidos.model.dto.entrega.SobranteDto;
import com.mateoiriso.pedidos.model.dto.pedido.PedidoDto;
import com.mateoiriso.pedidos.model.dto.reparto.RepartoDto;
import com.mateoiriso.pedidos.model.dto.reparto.RequestRepartoDto;
import com.mateoiriso.pedidos.service.IEntregaService;
import com.mateoiriso.pedidos.service.IRepartoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Repartos")
@RestController
@RequestMapping("/api/v1/repartos")
@RequiredArgsConstructor
public class RepartoController {

    private final IRepartoService repartoService;
    private final IEntregaService entregaService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    ResponseEntity<RepartoDto> crear(@Valid @RequestBody RequestRepartoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repartoService.crear(dto));
    }

    @PreAuthorize("hasRole('ADMIN') or @seguridad.esRepartidorDelReparto(#id, authentication)")
    @GetMapping("{id}")
    ResponseEntity<RepartoDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(repartoService.findById(id));
    }

    @Operation(summary = "Asignar un pedido pendiente de la misma zona al reparto")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("{id}/pedidos/{pedidoId}")
    ResponseEntity<PedidoDto> asignarPedido(@PathVariable Integer id, @PathVariable Integer pedidoId) {
        return ResponseEntity.ok(repartoService.asignarPedido(id, pedidoId));
    }

    @PreAuthorize("hasRole('ADMIN') or @seguridad.esRepartidorDelReparto(#id, authentication)")
    @GetMapping("{id}/pedidos")
    ResponseEntity<List<PedidoDto>> pedidos(@PathVariable Integer id) {
        return ResponseEntity.ok(repartoService.pedidosDelReparto(id));
    }

    @Operation(summary = "Lo que queda en la camioneta para ofrecer a otros clientes")
    @PreAuthorize("hasRole('ADMIN') or @seguridad.esRepartidorDelReparto(#id, authentication)")
    @GetMapping("{id}/sobrantes")
    ResponseEntity<List<SobranteDto>> sobrantes(@PathVariable Integer id) {
        return ResponseEntity.ok(entregaService.sobrantesDelReparto(id));
    }
}
