package com.ApiarioSamano.MicroServiceApiarios.service.MicroServicesAPI.MedicamentosServiceClient;

import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
import java.util.List;

public interface IMedicamentosService {
    List<MedicamentosResponse> obtenerTodos();

    MedicamentosResponse obtenerPorId(Long id);
}