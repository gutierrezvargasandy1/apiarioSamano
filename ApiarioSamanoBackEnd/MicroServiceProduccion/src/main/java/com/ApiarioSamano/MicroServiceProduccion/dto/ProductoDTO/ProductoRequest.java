package com.ApiarioSamano.MicroServiceProduccion.dto.ProductoDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoRequest {
    private String nombre;
    private BigDecimal precioMayoreo;
    private BigDecimal precioMenudeo;
    private byte[] foto;
    private String codigoBarras;
    private String tipoDeProducto;
    private Long idLote;
    private Boolean activo;
}
