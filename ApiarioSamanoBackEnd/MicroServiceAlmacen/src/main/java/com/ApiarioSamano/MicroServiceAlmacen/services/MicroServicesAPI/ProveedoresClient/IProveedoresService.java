package com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.ProveedoresClient;

import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;
import java.util.List;

public interface IProveedoresService {
    List<ProveedorResponseDTO> obtenerTodosProveedores();
}