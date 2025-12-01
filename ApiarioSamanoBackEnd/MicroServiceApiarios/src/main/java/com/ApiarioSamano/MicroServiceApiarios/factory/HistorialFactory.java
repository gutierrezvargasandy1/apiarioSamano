package com.ApiarioSamano.MicroServiceApiarios.factory;

import org.springframework.stereotype.Component;
import com.ApiarioSamano.MicroServiceApiarios.dto.HistorialMedicoDTO.HistorialMedicoDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.HistorialRecetasDTO.CrearHistorialRecetasRequest;
import com.ApiarioSamano.MicroServiceApiarios.model.HistorialMedico;
import com.ApiarioSamano.MicroServiceApiarios.model.HistorialRecetas;
import com.ApiarioSamano.MicroServiceApiarios.model.Receta;

@Component
public class HistorialFactory extends Factory<Object, Object> {

    @Override
    public Object crear(Object data) {

        if (data instanceof HistorialMedicoDTO dto) {
            return crearHistorialMedico(dto);
        }

        if (data instanceof CrearHistorialRecetasRequest req) {
            return crearHistorialRecetas(req.getHistorialMedico(), req.getReceta());
        }

        throw new IllegalArgumentException("Tipo de datos no soportado: " + data.getClass());
    }

    public HistorialMedico crearHistorialMedico(HistorialMedicoDTO dto) {
        HistorialMedico historial = new HistorialMedico();
        historial.setNotas(dto.getNotas());
        return historial;
    }

    public HistorialRecetas crearHistorialRecetas(HistorialMedico historialMedico, Receta receta) {
        if (historialMedico == null || receta == null) {
            throw new IllegalArgumentException("HistorialMedico y Receta son obligatorios");
        }

        HistorialRecetas historial = new HistorialRecetas();
        historial.setHistorialMedico(historialMedico);
        historial.setReceta(receta);
        return historial;
    }
}
