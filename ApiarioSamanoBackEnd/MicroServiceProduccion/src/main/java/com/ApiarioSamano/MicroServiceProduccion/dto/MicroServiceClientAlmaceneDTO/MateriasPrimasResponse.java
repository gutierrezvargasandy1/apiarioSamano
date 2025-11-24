package com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientAlmaceneDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MateriasPrimasResponse {
    private Long id;
    private String nombre;
    private byte[] foto;
    private BigDecimal cantidad;
    private Integer idProveedor;
}
