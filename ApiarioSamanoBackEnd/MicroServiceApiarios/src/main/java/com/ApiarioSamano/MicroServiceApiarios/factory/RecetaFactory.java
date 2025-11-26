package com.ApiarioSamano.MicroServiceApiarios.factory;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO.RecetaRequest;
import com.ApiarioSamano.MicroServiceApiarios.model.Receta;
import com.ApiarioSamano.MicroServiceApiarios.model.RecetaMedicamento;

@Component
public class RecetaFactory implements Factory<Receta, RecetaRequest> {

    @Override
    public Receta crear(RecetaRequest dto) {

        Receta receta = new Receta();
        receta.setDescripcion(dto.getDescripcion());

        if (dto.getMedicamentos() != null) {
            receta.setMedicamentos(
                    dto.getMedicamentos().stream()
                            .map(this::convertirMedicamento)
                            .collect(Collectors.toList()));

            // Asignamos la receta a cada RecetaMedicamento
            receta.getMedicamentos().forEach(m -> m.setReceta(receta));
        }

        return receta;
    }

    private RecetaMedicamento convertirMedicamento(MedicamentosRequestDTO med) {
        RecetaMedicamento rm = new RecetaMedicamento();
        rm.setIdMedicamento(med.getId());
        return rm;
    }
}
