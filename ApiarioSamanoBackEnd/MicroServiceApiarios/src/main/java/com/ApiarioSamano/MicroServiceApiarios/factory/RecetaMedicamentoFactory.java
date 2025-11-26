package com.ApiarioSamano.MicroServiceApiarios.factory;

import org.springframework.stereotype.Component;

import com.ApiarioSamano.MicroServiceApiarios.dto.ResetaMedicamentoDTO.RecetaMedicamentoDTO;
import com.ApiarioSamano.MicroServiceApiarios.model.RecetaMedicamento;

@Component
public class RecetaMedicamentoFactory implements Factory<RecetaMedicamento, RecetaMedicamentoDTO> {

    @Override
    public RecetaMedicamento crear(RecetaMedicamentoDTO data) {

        RecetaMedicamento rm = new RecetaMedicamento();

        rm.setIdMedicamento(data.getIdMedicamento());
        rm.setMedicamentoInfo(data.getMedicamentoInfo());

        return rm;
    }
}