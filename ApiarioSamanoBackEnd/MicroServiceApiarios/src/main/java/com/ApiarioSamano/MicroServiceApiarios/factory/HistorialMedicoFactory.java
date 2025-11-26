package com.ApiarioSamano.MicroServiceApiarios.factory;

import org.springframework.stereotype.Component;

import com.ApiarioSamano.MicroServiceApiarios.dto.HistorialMedicoDTO.HistorialMedicoDTO;
import com.ApiarioSamano.MicroServiceApiarios.model.HistorialMedico;

@Component
public class HistorialMedicoFactory implements Factory<HistorialMedico, HistorialMedicoDTO> {

    @Override
    public HistorialMedico crear(HistorialMedicoDTO data) {
        HistorialMedico historial = new HistorialMedico();
        historial.setNotas(data.getNotas());
        return historial;
    }

}
