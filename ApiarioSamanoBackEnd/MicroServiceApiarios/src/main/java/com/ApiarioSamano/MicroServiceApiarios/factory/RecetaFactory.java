package com.ApiarioSamano.MicroServiceApiarios.factory;

import org.springframework.stereotype.Component;
import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO.RecetaRequest;
import com.ApiarioSamano.MicroServiceApiarios.dto.ResetaMedicamentoDTO.RecetaMedicamentoDTO;
import com.ApiarioSamano.MicroServiceApiarios.model.Receta;
import com.ApiarioSamano.MicroServiceApiarios.model.RecetaMedicamento;

import java.util.stream.Collectors;

@Component
public class RecetaFactory extends Factory<Object, Object> {

    @Override
    public Object crear(Object data) {

        if (data instanceof RecetaRequest dto) {
            return crearReceta(dto);
        }

        if (data instanceof RecetaMedicamentoDTO dto) {
            return crearRecetaMedicamento(dto);
        }

        throw new IllegalArgumentException("Tipo de datos no soportado: " + data.getClass());
    }

    public Receta crearReceta(RecetaRequest dto) {
        Receta receta = new Receta();
        receta.setDescripcion(dto.getDescripcion());

        if (dto.getMedicamentos() != null) {
            receta.setMedicamentos(
                    dto.getMedicamentos()
                            .stream()
                            .map(m -> crearRecetaMedicamento(m.getId()))
                            .collect(Collectors.toList()));

            receta.getMedicamentos().forEach(m -> m.setReceta(receta));
        }

        return receta;
    }

    public RecetaMedicamento crearRecetaMedicamento(RecetaMedicamentoDTO dto) {
        RecetaMedicamento rm = new RecetaMedicamento();
        rm.setIdMedicamento(dto.getIdMedicamento());
        rm.setMedicamentoInfo(dto.getMedicamentoInfo());
        return rm;
    }

    public RecetaMedicamento crearRecetaMedicamento(Long idMedicamento) {
        RecetaMedicamento rm = new RecetaMedicamento();
        rm.setIdMedicamento(idMedicamento);
        return rm;
    }

    public RecetaMedicamento crearRecetaMedicamento(Long idMedicamento, MedicamentosResponse info) {
        RecetaMedicamento rm = new RecetaMedicamento();
        rm.setIdMedicamento(idMedicamento);
        rm.setMedicamentoInfo(info);
        return rm;
    }
}
