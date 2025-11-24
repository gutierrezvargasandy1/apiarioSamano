package com.ApiarioSamano.MicroServiceProveedores.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodigoResponse<T> {
    private int statusCode;
    private String message;
    private T data;
}
