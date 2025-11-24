package com.ApiarioSamano.MicroServiceGeneradorCodigo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoteRequest {
    private String producto;
    private int numeroLote;
}
