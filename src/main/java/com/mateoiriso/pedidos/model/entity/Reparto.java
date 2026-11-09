package com.mateoiriso.pedidos.model.entity;

import com.mateoiriso.pedidos.model.enums.EEstadoReparto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/** Una salida de un repartidor a una zona en un día. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "reparto")
public class Reparto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reparto")
    public Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_repartidor", referencedColumnName = "id_usuario")
    public Usuario repartidor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_zona", referencedColumnName = "id_zona")
    public Zona zona;

    @Column(name = "fecha", nullable = false)
    public LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    public EEstadoReparto estado;
}
