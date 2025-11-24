package com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientAlmaceneDTO;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlmacenResponse {
    private Long id;
    private String numeroSeguimiento;
    private String ubicacion;
    private Integer capacidad;

    private List<MateriasPrimasResponse> materiasPrimas;
    private List<HerramientasResponse> herramientas;
    private List<MedicamentosResponse> medicamentos;
}
