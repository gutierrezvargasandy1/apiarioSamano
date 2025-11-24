package com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientAlmaceneDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HerramientasResponse {
    private Long id;
    private String nombre;
    private byte[] foto;
    private Integer idProveedor;
}
