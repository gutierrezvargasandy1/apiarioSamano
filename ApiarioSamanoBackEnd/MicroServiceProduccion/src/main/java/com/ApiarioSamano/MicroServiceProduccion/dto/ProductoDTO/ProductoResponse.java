package com.ApiarioSamano.MicroServiceProduccion.dto.ProductoDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponse {
    private Long id;
    private String nombre;
    private BigDecimal precioMayoreo;
    private BigDecimal precioMenudeo;
    private String fotoBase64;
    private String codigoBarras;
    private String tipoDeProducto;
    private Long idLote;
    private String numeroSeguimientoLote;
    private String tipoProductoLote;
    private Boolean activo;
    private String fechaCreacion;
    private String fechaActualizacion;
}
