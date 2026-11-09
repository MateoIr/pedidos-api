package com.mateoiriso.pedidos;

import com.mateoiriso.pedidos.model.dto.entrega.RequestEntregaDto;
import com.mateoiriso.pedidos.model.dto.entrega.RequestEntregaItemDto;
import com.mateoiriso.pedidos.model.dto.entrega.RequestSobranteDto;
import com.mateoiriso.pedidos.model.dto.pedido.PedidoDetalleDto;
import com.mateoiriso.pedidos.model.dto.pedido.PedidoDto;
import com.mateoiriso.pedidos.model.dto.pedido.RequestItemDto;
import com.mateoiriso.pedidos.model.dto.pedido.RequestPedidoDto;
import com.mateoiriso.pedidos.model.dto.producto.ProductoDto;
import com.mateoiriso.pedidos.model.dto.producto.RequestProductoDto;
import com.mateoiriso.pedidos.model.dto.reparto.RepartoDto;
import com.mateoiriso.pedidos.model.dto.reparto.RequestRepartoDto;
import com.mateoiriso.pedidos.model.dto.zona.RequestZonaDto;
import com.mateoiriso.pedidos.model.entity.Usuario;
import com.mateoiriso.pedidos.model.enums.EEstadoPedido;
import com.mateoiriso.pedidos.model.enums.ERol;
import com.mateoiriso.pedidos.model.repository.UsuarioRepository;
import com.mateoiriso.pedidos.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Recorre un día de reparto completo contra la base real (H2 + migraciones Flyway):
 * un cliente rechaza parte de su pedido, sube el precio, y el sobrante se le ofrece a otro cliente.
 */
@SpringBootTest
@Transactional
class FlujoRepartoIntegrationTest {

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired IZonaService zonaService;
    @Autowired IProductoService productoService;
    @Autowired IPedidoService pedidoService;
    @Autowired IRepartoService repartoService;
    @Autowired IEntregaService entregaService;

    @Test
    void sobranteDeUnaEntregaParcialSeVendeAOtroClienteConLaReglaDePrecios() {
        Integer zona = zonaService.save(new RequestZonaDto("Centro")).id;
        ProductoDto agua = productoService.save(new RequestProductoDto("Agua 2L", new BigDecimal("1000.00")));
        ProductoDto gaseosa = productoService.save(new RequestProductoDto("Gaseosa 1.5L", new BigDecimal("2000.00")));
        Integer repartidor = usuario("Repartidor", "rep@test.com", ERol.REPARTIDOR);
        Integer ana = usuario("Ana", "ana@test.com", ERol.CLIENTE);
        Integer bruno = usuario("Bruno", "bruno@test.com", ERol.CLIENTE);

        // Ana pide 5 aguas y 2 gaseosas; Bruno pide 1 agua. Precios del momento: 1000 y 2000.
        PedidoDto pedidoAna = pedidoService.crear(ana, new RequestPedidoDto(zona,
                List.of(new RequestItemDto(agua.id, 5), new RequestItemDto(gaseosa.id, 2))));
        PedidoDto pedidoBruno = pedidoService.crear(bruno, new RequestPedidoDto(zona,
                List.of(new RequestItemDto(agua.id, 1))));

        RepartoDto reparto = repartoService.crear(new RequestRepartoDto(repartidor, zona, LocalDate.now()));
        repartoService.asignarPedido(reparto.id, pedidoAna.id);
        repartoService.asignarPedido(reparto.id, pedidoBruno.id);

        // Ana recibe solo 3 aguas y rechaza las gaseosas: sobran 2 aguas y 2 gaseosas.
        PedidoDto entregadoAna = entregaService.registrarEntrega(pedidoAna.id, new RequestEntregaDto(List.of(
                new RequestEntregaItemDto(lineaDe(pedidoAna, agua.id).id, 3),
                new RequestEntregaItemDto(lineaDe(pedidoAna, gaseosa.id).id, 0))));
        assertThat(entregadoAna.estado).isEqualTo(EEstadoPedido.PARCIAL);
        assertThat(entregadoAna.total).isEqualByComparingTo("3000.00");
        assertThat(entregaService.sobrantesDelReparto(reparto.id)).hasSize(2);

        // Durante el día aumentan los dos productos.
        productoService.update(agua.id, new RequestProductoDto("Agua 2L", new BigDecimal("1200.00")));
        productoService.update(gaseosa.id, new RequestProductoDto("Gaseosa 1.5L", new BigDecimal("2500.00")));

        // A Bruno se le ofrecen los sobrantes.
        entregaService.agregarSobrante(pedidoBruno.id, new RequestSobranteDto(agua.id, 2));
        PedidoDto bruno2 = entregaService.agregarSobrante(pedidoBruno.id, new RequestSobranteDto(gaseosa.id, 1));

        // Agua ya estaba en su pedido -> 3 unidades al precio original 1000.
        PedidoDetalleDto aguaBruno = lineaDe(bruno2, agua.id);
        assertThat(aguaBruno.cantidad).isEqualTo(3);
        assertThat(aguaBruno.precioUnitario).isEqualByComparingTo("1000.00");
        // Gaseosa no estaba -> precio vigente 2500.
        PedidoDetalleDto gaseosaBruno = lineaDe(bruno2, gaseosa.id);
        assertThat(gaseosaBruno.precioUnitario).isEqualByComparingTo("2500.00");
        assertThat(gaseosaBruno.agregadoDeSobrante).isTrue();
        assertThat(bruno2.total).isEqualByComparingTo("5500.00");

        // En la camioneta queda solo 1 gaseosa (las aguas se agotaron y su registro se borró).
        assertThat(entregaService.sobrantesDelReparto(reparto.id))
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.producto.id).isEqualTo(gaseosa.id);
                    assertThat(s.cantidad).isEqualTo(1);
                });
    }

    private Integer usuario(String nombre, String email, ERol rol) {
        return usuarioRepository.save(new Usuario(null, nombre, email, "hash-de-prueba", rol)).id;
    }

    private PedidoDetalleDto lineaDe(PedidoDto pedido, Integer productoId) {
        return pedido.detalles.stream().filter(d -> d.producto.id.equals(productoId)).findFirst().orElseThrow();
    }
}
