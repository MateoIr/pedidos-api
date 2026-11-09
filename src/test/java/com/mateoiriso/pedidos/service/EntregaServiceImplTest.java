package com.mateoiriso.pedidos.service;

import com.mateoiriso.pedidos.exceptions.BusinessException;
import com.mateoiriso.pedidos.model.dto.entrega.RequestEntregaDto;
import com.mateoiriso.pedidos.model.dto.entrega.RequestEntregaItemDto;
import com.mateoiriso.pedidos.model.dto.entrega.RequestSobranteDto;
import com.mateoiriso.pedidos.model.entity.*;
import com.mateoiriso.pedidos.model.enums.EEstadoPedido;
import com.mateoiriso.pedidos.model.enums.EEstadoReparto;
import com.mateoiriso.pedidos.model.enums.ERol;
import com.mateoiriso.pedidos.model.mappers.entrega.SobranteMapper;
import com.mateoiriso.pedidos.model.repository.*;
import com.mateoiriso.pedidos.service.Impl.EntregaServiceImpl;
import com.mateoiriso.pedidos.service.Impl.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntregaServiceImplTest {

    @Mock PedidoRepository pedidoRepository;
    @Mock PedidoDetalleRepository detalleRepository;
    @Mock SobranteRepository sobranteRepository;
    @Mock ProductoRepository productoRepository;
    @Mock RepartoRepository repartoRepository;
    @Mock SobranteMapper sobranteMapper;
    @Mock PedidoServiceImpl pedidoService;

    @InjectMocks EntregaServiceImpl service;

    Reparto reparto;
    Pedido pedido;
    Producto agua;
    Producto gaseosa;

    @BeforeEach
    void setUp() {
        Zona zona = new Zona(1, "Centro");
        Usuario repartidor = new Usuario(2, "Repartidor", "r@test.com", "hash", ERol.REPARTIDOR);
        Usuario cliente = new Usuario(3, "Cliente", "c@test.com", "hash", ERol.CLIENTE);
        reparto = new Reparto(10, repartidor, zona, LocalDate.now(), EEstadoReparto.ABIERTO);
        pedido = new Pedido(100, cliente, zona, reparto, EEstadoPedido.ASIGNADO, LocalDateTime.now());
        agua = new Producto(1, "Agua 2L", new BigDecimal("1000.00"), true);
        gaseosa = new Producto(2, "Gaseosa 1.5L", new BigDecimal("2000.00"), true);
        lenient().when(pedidoRepository.findById(100)).thenReturn(Optional.of(pedido));
        lenient().when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PedidoDetalle linea(int id, Producto producto, int cantidad, String precio) {
        return new PedidoDetalle(id, pedido, producto, cantidad, new BigDecimal(precio), null, false);
    }

    private RequestEntregaDto entrega(int detalleId, int entregada) {
        return new RequestEntregaDto(List.of(new RequestEntregaItemDto(detalleId, entregada)));
    }

    @Nested
    @DisplayName("registrarEntrega")
    class RegistrarEntrega {

        @Test
        @DisplayName("si se entrega todo, el pedido queda ENTREGADO y no genera sobrantes")
        void entregaCompleta() {
            PedidoDetalle detalle = linea(1, agua, 5, "1000.00");
            when(detalleRepository.findByPedidoId(100)).thenReturn(List.of(detalle));

            service.registrarEntrega(100, entrega(1, 5));

            assertThat(pedido.estado).isEqualTo(EEstadoPedido.ENTREGADO);
            assertThat(detalle.cantidadEntregada).isEqualTo(5);
            verify(sobranteRepository, never()).save(any());
        }

        @Test
        @DisplayName("si se entrega una parte, el pedido queda PARCIAL y la diferencia pasa a sobrantes")
        void entregaParcial() {
            when(detalleRepository.findByPedidoId(100)).thenReturn(List.of(linea(1, agua, 5, "1000.00")));
            when(sobranteRepository.findByRepartoIdAndProductoId(10, 1)).thenReturn(Optional.empty());

            service.registrarEntrega(100, entrega(1, 3));

            assertThat(pedido.estado).isEqualTo(EEstadoPedido.PARCIAL);
            ArgumentCaptor<Sobrante> captor = ArgumentCaptor.forClass(Sobrante.class);
            verify(sobranteRepository).save(captor.capture());
            assertThat(captor.getValue().cantidad).isEqualTo(2);
            assertThat(captor.getValue().producto).isEqualTo(agua);
        }

        @Test
        @DisplayName("si ya había sobrante de ese producto, se suma al existente")
        void sumaAlSobranteExistente() {
            Sobrante existente = new Sobrante(50, reparto, agua, 4);
            when(detalleRepository.findByPedidoId(100)).thenReturn(List.of(linea(1, agua, 5, "1000.00")));
            when(sobranteRepository.findByRepartoIdAndProductoId(10, 1)).thenReturn(Optional.of(existente));

            service.registrarEntrega(100, entrega(1, 3));

            assertThat(existente.cantidad).isEqualTo(6);
        }

        @Test
        @DisplayName("no se pueden entregar más unidades que las pedidas")
        void noMasQueLoPedido() {
            when(detalleRepository.findByPedidoId(100)).thenReturn(List.of(linea(1, agua, 5, "1000.00")));

            assertThatThrownBy(() -> service.registrarEntrega(100, entrega(1, 6)))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("hay que informar todas las líneas del pedido")
        void faltanLineas() {
            when(detalleRepository.findByPedidoId(100))
                    .thenReturn(List.of(linea(1, agua, 5, "1000.00"), linea(2, gaseosa, 1, "2000.00")));

            assertThatThrownBy(() -> service.registrarEntrega(100, entrega(1, 5)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("todas las líneas");
        }

        @Test
        @DisplayName("no se puede registrar la entrega de un pedido que no está en reparto")
        void pedidoNoAsignado() {
            pedido.estado = EEstadoPedido.PENDIENTE;

            assertThatThrownBy(() -> service.registrarEntrega(100, entrega(1, 5)))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Test
    @DisplayName("cancelar deja el pedido CANCELADO y manda todo a sobrantes")
    void cancelar() {
        when(detalleRepository.findByPedidoId(100)).thenReturn(List.of(linea(1, agua, 5, "1000.00")));
        when(sobranteRepository.findByRepartoIdAndProductoId(10, 1)).thenReturn(Optional.empty());

        service.cancelar(100);

        assertThat(pedido.estado).isEqualTo(EEstadoPedido.CANCELADO);
        ArgumentCaptor<Sobrante> captor = ArgumentCaptor.forClass(Sobrante.class);
        verify(sobranteRepository).save(captor.capture());
        assertThat(captor.getValue().cantidad).isEqualTo(5);
    }

    @Nested
    @DisplayName("agregarSobrante: regla de precios")
    class AgregarSobrante {

        @Test
        @DisplayName("si el producto ya estaba en el pedido, se respeta el precio original aunque haya aumentado")
        void productoQueYaEstabaRespetaPrecioViejo() {
            agua.precio = new BigDecimal("1500.00"); // aumentó después del pedido
            PedidoDetalle original = linea(1, agua, 2, "1000.00");
            when(sobranteRepository.findByRepartoIdAndProductoId(10, 1))
                    .thenReturn(Optional.of(new Sobrante(50, reparto, agua, 3)));
            when(detalleRepository.findByPedidoIdAndProductoId(100, 1)).thenReturn(Optional.of(original));

            service.agregarSobrante(100, new RequestSobranteDto(1, 3));

            assertThat(original.cantidad).isEqualTo(5);
            assertThat(original.precioUnitario).isEqualByComparingTo("1000.00");
        }

        @Test
        @DisplayName("si el producto no estaba en el pedido, se cobra el precio vigente")
        void productoNuevoCobraPrecioVigente() {
            gaseosa.precio = new BigDecimal("2500.00"); // precio vigente, con aumento
            when(sobranteRepository.findByRepartoIdAndProductoId(10, 2))
                    .thenReturn(Optional.of(new Sobrante(51, reparto, gaseosa, 4)));
            when(detalleRepository.findByPedidoIdAndProductoId(100, 2)).thenReturn(Optional.empty());
            when(productoRepository.findById(2)).thenReturn(Optional.of(gaseosa));

            service.agregarSobrante(100, new RequestSobranteDto(2, 1));

            ArgumentCaptor<PedidoDetalle> captor = ArgumentCaptor.forClass(PedidoDetalle.class);
            verify(detalleRepository).save(captor.capture());
            PedidoDetalle nueva = captor.getValue();
            assertThat(nueva.precioUnitario).isEqualByComparingTo("2500.00");
            assertThat(nueva.cantidad).isEqualTo(1);
            assertThat(nueva.agregadoDeSobrante).isTrue();
        }

        @Test
        @DisplayName("descuenta del sobrante y lo borra cuando llega a cero")
        void descuentaYBorraSobrante() {
            Sobrante sobrante = new Sobrante(50, reparto, agua, 2);
            when(sobranteRepository.findByRepartoIdAndProductoId(10, 1)).thenReturn(Optional.of(sobrante));
            when(detalleRepository.findByPedidoIdAndProductoId(100, 1)).thenReturn(Optional.of(linea(1, agua, 1, "1000.00")));

            service.agregarSobrante(100, new RequestSobranteDto(1, 2));

            verify(sobranteRepository).delete(sobrante);
        }

        @Test
        @DisplayName("no se puede agregar más de lo que sobró")
        void sobranteInsuficiente() {
            when(sobranteRepository.findByRepartoIdAndProductoId(10, 1))
                    .thenReturn(Optional.of(new Sobrante(50, reparto, agua, 1)));

            assertThatThrownBy(() -> service.agregarSobrante(100, new RequestSobranteDto(1, 2)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Solo quedan 1");
            verify(detalleRepository, never()).save(any());
        }

        @Test
        @DisplayName("no se puede usar un sobrante que no existe en el reparto del pedido")
        void sinSobrante() {
            when(sobranteRepository.findByRepartoIdAndProductoId(10, 1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.agregarSobrante(100, new RequestSobranteDto(1, 1)))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
