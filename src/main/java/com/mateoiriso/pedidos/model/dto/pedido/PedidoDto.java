package com.mateoiriso.pedidos.model.dto.pedido;

import com.mateoiriso.pedidos.model.dto.usuario.UsuarioDto;
import com.mateoiriso.pedidos.model.dto.zona.ZonaDto;
import com.mateoiriso.pedidos.model.enums.EEstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PedidoDto {
    public Integer id;
    public UsuarioDto cliente;
    public ZonaDto zona;
    public Integer repartoId;
    public EEstadoPedido estado;
    public LocalDateTime fechaCreacion;
    public List<PedidoDetalleDto> detalles;
    public BigDecimal total;
}
