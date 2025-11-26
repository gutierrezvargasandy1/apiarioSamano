package com.ApiarioSamano.MicroServiceApiarios.factory;

import org.springframework.stereotype.Component;
import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.model.Apiarios;

@Component
public class ApiariosFactory implements Factory<Apiarios, ApiarioRequestDTO> {

  @Override
  public Apiarios crear(ApiarioRequestDTO dto) {
    Apiarios apiario = new Apiarios();
    apiario.setNumeroApiario(dto.getNumeroApiario());
    apiario.setUbicacion(dto.getUbicacion());
    apiario.setSalud(dto.getSalud());
    apiario.setDispositivoId(dto.getDispositivoId());

    // 🔥 CORRECCIÓN: NO crear HistorialMedico aquí
    // El historial se creará después cuando sea necesario
    apiario.setHistorialMedico(null);
    apiario.setReceta(null);

    return apiario;
  }
}