package com.mateoiriso.pedidos.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    public Integer id;

    @Column(name = "nombre", nullable = false, length = 150)
    public String nombre;

    /** Precio vigente. Cada pedido guarda su propia copia en PedidoDetalle. */
    @Column(name = "precio", nullable = false, precision = 20, scale = 2)
    public BigDecimal precio;

    @Column(name = "activo", nullable = false)
    public Boolean activo;
}
