package com.ApiarioSamano.MicroServiceUsuario.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "apellido_pa", nullable = false, length = 100)
    private String apellidoPa;

    @Column(name = "apellido_ma", nullable = false, length = 100)
    private String apellidoMa;

    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(nullable = false, length = 50)
    private String rol;

    @Column(nullable = false)
    private boolean estado;

}
