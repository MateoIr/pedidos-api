package com.mateoiriso.pedidos.service;

import com.mateoiriso.pedidos.exceptions.IaNoDisponibleException;
import com.mateoiriso.pedidos.model.dto.entrega.SugerenciaSobranteDto;
import com.mateoiriso.pedidos.model.dto.entrega.SugerenciasDto;
import com.mateoiriso.pedidos.model.entity.*;
import com.mateoiriso.pedidos.model.enums.EEstadoPedido;
import com.mateoiriso.pedidos.model.enums.EEstadoReparto;
import com.mateoiriso.pedidos.model.enums.ERol;
import com.mateoiriso.pedidos.model.repository.PedidoDetalleRepository;
import com.mateoiriso.pedidos.model.repository.PedidoRepository;
import com.mateoiriso.pedidos.model.repository.RepartoRepository;
import com.mateoiriso.pedidos.model.repository.SobranteRepository;
import com.mateoiriso.pedidos.service.Impl.SugerenciaServiceImpl;
import com.mateoiriso.pedidos.service.ia.ISugerenciaIaClient;
import com.mateoiriso.pedidos.service.ia.RespuestaSugerenciasIa;
import com.mateoiriso.pedidos.service.ia.RespuestaSugerenciasIa.SugerenciaIa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** La IA está mockeada: lo que se prueba es que el sistema no confía ciegamente en su respuesta. */
@ExtendWith(MockitoExtension.class)
class SugerenciaServiceImplTest {

    @Mock RepartoRepository repartoRepository;
    @Mock PedidoRepository pedidoRepository;
    @Mock PedidoDetalleRepository detalleRepository;
    @Mock SobranteRepository sobranteRepository;
    @Mock ObjectProvider<ISugerenciaIaClient> iaProvider;
    @Mock ISugerenciaIaClient ia;

    @InjectMocks SugerenciaServiceImpl service;

    Reparto reparto;
    Producto agua;
    Producto gaseosa;
    Pedido pedidoBruno;
    Pedido pedidoCarla;

    @BeforeEach
    void setUp() {
        Zona zona = new Zona(1, "Centro");
        reparto = new Reparto(10, new Usuario(2, "Rep", "r@t.com", "h", ERol.REPARTIDOR), zona, LocalDate.now(), EEstadoReparto.ABIERTO);
        agua = new Producto(1, "Agua 2L", new BigDecimal("1200.00"), true);       // precio vigente, ya aumentó
        gaseosa = new Producto(2, "Gaseosa 1.5L", new BigDecimal("2500.00"), true);
        pedidoBruno = new Pedido(100, new Usuario(3, "Bruno", "b@t.com", "h", ERol.CLIENTE), zona, reparto, EEstadoPedido.ASIGNADO, LocalDateTime.now());
        pedidoCarla = new Pedido(101, new Usuario(4, "Carla", "c@t.com", "h", ERol.CLIENTE), zona, reparto, EEstadoPedido.ASIGNADO, LocalDateTime.now());
        Pedido entregado = new Pedido(102, new Usuario(5, "Dani", "d@t.com", "h", ERol.CLIENTE), zona, reparto, EEstadoPedido.ENTREGADO, LocalDateTime.now());

        lenient().when(repartoRepository.existsById(10)).thenReturn(true);
        lenient().when(iaProvider.getIfAvailable()).thenReturn(ia);
        lenient().when(ia.modelo()).thenReturn("claude-opus-5-5");
        lenient().when(sobranteRepository.findByRepartoId(10)).thenReturn(List.of(
                new Sobrante(50, reparto, agua, 2), new Sobrante(51, reparto, gaseosa, 1)));
        lenient().when(pedidoRepository.findByRepartoId(10)).thenReturn(List.of(pedidoBruno, pedidoCarla, entregado));
        // Bruno ya había pedido agua a $1000; Carla solo pidió gaseosa.
        lenient().when(detalleRepository.findByPedidoId(100)).thenReturn(List.of(
                new PedidoDetalle(1, pedidoBruno, agua, 1, new BigDecimal("1000.00"), null, false)));
        lenient().when(detalleRepository.findByPedidoId(101)).thenReturn(List.of(
                new PedidoDetalle(2, pedidoCarla, gaseosa, 2, new BigDecimal("2000.00"), null, false)));
    }

    private void iaResponde(SugerenciaIa... sugerencias) {
        when(ia.sugerir(anyString())).thenReturn(new RespuestaSugerenciasIa(List.of(sugerencias)));
    }

    @Test
    @DisplayName("el precio lo calcula el sistema con la regla de sobrantes, no la IA")
    void precioSegunRegla() {
        iaResponde(new SugerenciaIa(100, 1, 2, "Bruno ya pide agua"),
                new SugerenciaIa(101, 2, 1, "Carla ya pide gaseosa"));

        SugerenciasDto dto = service.sugerirSobrantes(10);

        assertThat(dto.sugerencias).hasSize(2);
        SugerenciaSobranteDto aBruno = dto.sugerencias.get(0);
        assertThat(aBruno.cliente).isEqualTo("Bruno");
        assertThat(aBruno.precioUnitario).isEqualByComparingTo("1000.00"); // su precio original
        assertThat(aBruno.respetaPrecioOriginal).isTrue();
        assertThat(dto.descartadas).isEmpty();
    }

    @Test
    @DisplayName("un producto que el cliente no había pedido va al precio vigente")
    void productoNuevoPrecioVigente() {
        iaResponde(new SugerenciaIa(101, 1, 1, "Ofrecer agua a Carla"));

        SugerenciaSobranteDto s = service.sugerirSobrantes(10).sugerencias.get(0);

        assertThat(s.precioUnitario).isEqualByComparingTo("1200.00");
        assertThat(s.respetaPrecioOriginal).isFalse();
    }

    @Test
    @DisplayName("descarta pedidos y productos que la IA inventó o que no corresponden")
    void descartaDatosInvalidos() {
        iaResponde(new SugerenciaIa(999, 1, 1, "pedido inexistente"),
                new SugerenciaIa(102, 1, 1, "pedido ya entregado"),
                new SugerenciaIa(100, 7, 1, "producto que no sobró"));

        SugerenciasDto dto = service.sugerirSobrantes(10);

        assertThat(dto.sugerencias).isEmpty();
        assertThat(dto.descartadas).hasSize(3);
    }

    @Test
    @DisplayName("no deja sugerir más unidades de las que quedan, sumando todas las sugerencias")
    void respetaStockAcumulado() {
        iaResponde(new SugerenciaIa(100, 1, 2, "primero"),
                new SugerenciaIa(101, 1, 1, "segundo: ya no queda agua"));

        SugerenciasDto dto = service.sugerirSobrantes(10);

        assertThat(dto.sugerencias).extracting(s -> s.pedidoId).containsExactly(100);
        assertThat(dto.descartadas).singleElement().asString().contains("quedan 0");
    }

    @Test
    @DisplayName("a la IA solo le llegan IDs y productos, nunca datos personales de los clientes")
    void noEnviaDatosPersonales() {
        iaResponde();

        service.sugerirSobrantes(10);

        ArgumentCaptor<String> contexto = ArgumentCaptor.forClass(String.class);
        verify(ia).sugerir(contexto.capture());
        assertThat(contexto.getValue())
                .contains("pedidoId 100", "Agua 2L")
                .doesNotContain("Bruno", "b@t.com", "Carla")
                .doesNotContain("pedidoId 102"); // el pedido ya entregado no se ofrece
    }

    @Test
    @DisplayName("si no hay sobrantes no llama a la IA")
    void sinSobrantesNoLlamaALaIa() {
        when(sobranteRepository.findByRepartoId(10)).thenReturn(List.of());

        assertThat(service.sugerirSobrantes(10).sugerencias).isEmpty();
        verify(ia, never()).sugerir(anyString());
    }

    @Test
    @DisplayName("si la IA no está habilitada responde 503 en vez de romper")
    void iaDeshabilitada() {
        when(iaProvider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> service.sugerirSobrantes(10)).isInstanceOf(IaNoDisponibleException.class);
    }
}
