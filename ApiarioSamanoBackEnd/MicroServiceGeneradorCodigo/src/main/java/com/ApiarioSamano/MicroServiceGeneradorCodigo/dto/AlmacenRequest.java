package com.ApiarioSamano.MicroServiceGeneradorCodigo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenRequest {
    private String zona;
    private String producto;
}