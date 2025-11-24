package com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO;

import java.util.List;

import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosRequestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecetaRequest {
    private String descripcion;
    private List<MedicamentosRequestDTO> medicamentos;

}
