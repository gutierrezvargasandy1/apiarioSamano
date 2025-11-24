package com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO;

import java.util.List;

import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamentosResponse;

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
