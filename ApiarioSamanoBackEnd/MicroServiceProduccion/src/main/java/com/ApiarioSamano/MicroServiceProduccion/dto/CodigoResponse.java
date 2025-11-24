package com.ApiarioSamano.MicroServiceProduccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodigoResponse<T> {
    private int codigo;
    private String descripcion;
    private T data;
}
