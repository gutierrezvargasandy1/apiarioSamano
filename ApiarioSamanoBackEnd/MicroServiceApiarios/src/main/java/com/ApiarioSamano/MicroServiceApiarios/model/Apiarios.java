package com.ApiarioSamano.MicroServiceApiarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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

    // ===========================
    // CAMPOS PARA DISPOSITIVO IOT
    // ===========================

    @Column(name = "dispositivo_id", length = 50, unique = true)
    private String dispositivoId; // ID único del ESP32 (MAC address en hex)

    @Column(name = "fecha_vinculacion")
    private LocalDateTime fechaVinculacion; // Cuando se vinculó el dispositivo al apiario
}