package com.ApiarioSamano.MicroServiceProduccion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Cosechas")
public class Cosecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_cosecha", nullable = false)
    private LocalDate fechaCosecha;

    @Column(length = 100)
    private String calidad;

    @Column(name = "tipo_cosecha", length = 100)
    private String tipoCosecha;

    @Column(precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "id_apiario")
    private Long idApiario;

    // Relación con Lote (muchas cosechas pueden pertenecer a un lote)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote", referencedColumnName = "id")
    @JsonIgnore
    private Lote lote;
}
