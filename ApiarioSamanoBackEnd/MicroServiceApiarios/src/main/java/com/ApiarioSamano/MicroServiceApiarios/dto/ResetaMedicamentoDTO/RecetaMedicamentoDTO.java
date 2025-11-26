package com.ApiarioSamano.MicroServiceApiarios.dto.ResetaMedicamentoDTO;

import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecetaMedicamentoDTO {
    private Long idMedicamento;
    private MedicamentosResponse medicamentoInfo;
}