package com.mateoiriso.pedidos.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Tabla intermedia entre Pedido y Producto, en lugar de un @ManyToMany,
 * porque la relación tiene datos propios: cantidad y precio al momento del pedido.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "pedido_detalle")
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido_detalle")
    public Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_pedido", referencedColumnName = "id_pedido")
    public Pedido pedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_producto", referencedColumnName = "id_producto")
    public Producto producto;

    @Column(name = "cantidad", nullable = false)
    public Integer cantidad;

    /** Precio congelado al crear el pedido: si el producto aumenta después, este pedido no cambia. */
    @Column(name = "precio_unitario", nullable = false, precision = 20, scale = 2)
    public BigDecimal precioUnitario;

    /** Cuánto se entregó realmente. Null mientras el pedido no se entrega. */
    @Column(name = "cantidad_entregada")
    public Integer cantidadEntregada;

    /** true si la línea se agregó durante el reparto con productos sobrantes de otro pedido. */
    @Column(name = "agregado_de_sobrante", nullable = false)
    public Boolean agregadoDeSobrante;
}
