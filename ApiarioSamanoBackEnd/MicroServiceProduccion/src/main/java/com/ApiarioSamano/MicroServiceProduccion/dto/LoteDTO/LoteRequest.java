package com.ApiarioSamano.MicroServiceProduccion.dto.LoteDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoteRequest {
    private Long id;
    private Long idAlmacen;
    private String tipoProducto;
}
