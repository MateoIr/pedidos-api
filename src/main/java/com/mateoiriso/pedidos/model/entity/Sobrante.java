package com.mateoiriso.pedidos.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Productos que quedaron en la camioneta durante un reparto, por una entrega
 * parcial o un pedido cancelado, y que se pueden ofrecer a los próximos clientes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "sobrante")
public class Sobrante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sobrante")
    public Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_reparto", referencedColumnName = "id_reparto")
    public Reparto reparto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_producto", referencedColumnName = "id_producto")
    public Producto producto;

    @Column(name = "cantidad", nullable = false)
    public Integer cantidad;
}
