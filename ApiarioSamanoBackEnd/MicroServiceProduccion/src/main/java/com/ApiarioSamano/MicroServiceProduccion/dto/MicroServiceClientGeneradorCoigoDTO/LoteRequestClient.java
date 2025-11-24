package com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientGeneradorCoigoDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoteRequestClient {
    private String producto;
    private int numeroLote;
}
