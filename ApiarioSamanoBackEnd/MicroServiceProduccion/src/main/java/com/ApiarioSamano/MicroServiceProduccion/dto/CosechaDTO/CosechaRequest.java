package com.ApiarioSamano.MicroServiceProduccion.dto.CosechaDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CosechaRequest {
    private Integer idLote;
    private String calidad;
    private String tipoCosecha;
    private BigDecimal cantidad;
    private Integer idApiario;
}
