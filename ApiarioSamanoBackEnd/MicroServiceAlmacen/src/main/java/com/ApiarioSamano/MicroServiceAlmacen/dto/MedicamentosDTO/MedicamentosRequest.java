package com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicamentosRequest {
    private String nombre;
    private Long id;
    private String descripcion;
    private Integer idAlmacen;
    private BigDecimal cantidad;
    private Integer idProveedor;
    private String foto;
}
