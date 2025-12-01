package com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.GeneradorCodigosClient;

import com.ApiarioSamano.MicroServiceAlmacen.dto.GeneradorCodigoClient.AlmacenRequestClient;

public interface IGeneradorCodigoService {
    String generarAlmacen(AlmacenRequestClient request);
}
