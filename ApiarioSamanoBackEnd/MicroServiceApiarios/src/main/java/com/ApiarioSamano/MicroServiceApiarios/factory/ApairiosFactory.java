package com.ApiarioSamano.MicroServiceApiarios.factory;

import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.model.Apiarios;
import com.ApiarioSamano.MicroServiceApiarios.model.HistorialMedico;

public class ApairiosFactory implements Factory<Apiarios,ApiarioRequestDTO> {
    
    @Override
    public Apiarios crear(ApiarioRequestDTO dto){
      Apiarios apiario = new Apiarios();
      apiario.setNumeroApiario(dto.getNumeroApiario());
      apiario.setUbicacion(dto.getUbicacion());
      apiario.setSalud(dto.getSalud());
      
      // crea un Historial inicial desde el principio
      HistorialMedico historialMedico = new HistorialMedico();

      historialMedico.setNotas("Nota inicial del apiario");
      apiario.setHistorialMedico(historialMedico);
      apiario.setReceta(null);

      return apiario;
    }






}
