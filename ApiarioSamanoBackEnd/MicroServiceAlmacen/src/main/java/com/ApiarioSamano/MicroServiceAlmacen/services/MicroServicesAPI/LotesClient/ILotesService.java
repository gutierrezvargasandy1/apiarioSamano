package com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.LotesClient;

import com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO.LoteResponseDTO;
import java.util.List;

public interface ILotesService {
    List<LoteResponseDTO> obtenerTodosLotes();

    List<LoteResponseDTO> obtenerLotesPorAlmacen(Long idAlmacen);

    LoteResponseDTO obtenerLotePorId(Long idLote);
}