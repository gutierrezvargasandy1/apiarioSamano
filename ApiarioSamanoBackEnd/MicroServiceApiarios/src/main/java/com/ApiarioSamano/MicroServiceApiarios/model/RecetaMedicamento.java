package com.ApiarioSamano.MicroServiceApiarios.model;

import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "recetamedicamento")
public class RecetaMedicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_receta")
    @JsonIgnore
    private Receta receta;

    @Column(name = "id_medicamento")
    private Long idMedicamento;

    @Transient
    private MedicamentosResponse medicamentoInfo;
}
