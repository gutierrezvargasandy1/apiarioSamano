package com.ApiarioSamano.MicroServiceAuth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodigoResponseDTO {
    private String codigo;
    private String estatus;
    private String descripcion;
}
