package com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoteRequest {
    private Long idAlmacen;
    private String tipoProducto;
}
