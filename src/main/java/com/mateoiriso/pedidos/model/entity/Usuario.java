package com.mateoiriso.pedidos.model.entity;

import com.mateoiriso.pedidos.model.enums.ERol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "passwordHash")
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    public Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    public String nombre;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    public ERol rol;
}
