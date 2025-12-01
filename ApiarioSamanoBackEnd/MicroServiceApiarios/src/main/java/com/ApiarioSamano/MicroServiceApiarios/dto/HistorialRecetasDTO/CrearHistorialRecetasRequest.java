package com.ApiarioSamano.MicroServiceApiarios.dto.HistorialRecetasDTO;

import com.ApiarioSamano.MicroServiceApiarios.model.HistorialMedico;
import com.ApiarioSamano.MicroServiceApiarios.model.Receta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrearHistorialRecetasRequest {
    public HistorialMedico historialMedico;
    public Receta receta;
}
