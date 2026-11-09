package com.mateoiriso.pedidos.model.entity;

import com.mateoiriso.pedidos.model.enums.EEstadoPedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    public Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_cliente", referencedColumnName = "id_usuario")
    public Usuario cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_zona", referencedColumnName = "id_zona")
    public Zona zona;

    /** Null hasta que el pedido se asigna a un reparto. */
    @ManyToOne
    @JoinColumn(name = "fk_reparto", referencedColumnName = "id_reparto")
    public Reparto reparto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    public EEstadoPedido estado;

    @Column(name = "fecha_creacion", nullable = false)
    public LocalDateTime fechaCreacion;
}
