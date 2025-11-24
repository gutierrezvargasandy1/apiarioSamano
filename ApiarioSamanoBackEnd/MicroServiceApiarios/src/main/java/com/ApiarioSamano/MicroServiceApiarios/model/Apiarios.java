package com.ApiarioSamano.MicroServiceApiarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "apiarios")
public class Apiarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_apiario", nullable = false)
    private Integer numeroApiario;

    @Column(name = "ubicacion", length = 200, nullable = false)
    private String ubicacion;

    @Column(name = "salud", length = 100)
    private String salud;

    @ManyToOne
    @JoinColumn(name = "id_receta")
    private Receta receta;

    @ManyToOne
    @JoinColumn(name = "id_historial_medico")
    private HistorialMedico historialMedico;
}