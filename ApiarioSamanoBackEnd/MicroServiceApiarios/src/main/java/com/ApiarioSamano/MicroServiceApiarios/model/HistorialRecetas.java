package com.ApiarioSamano.MicroServiceApiarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Historialrecetas")
public class HistorialRecetas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_historial_medico", nullable = false)
    private HistorialMedico historialMedico;

    @ManyToOne
    @JoinColumn(name = "id_receta", nullable = false)
    private Receta receta;
}