package com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicamentosResponse {
    private Long id;
    private String nombre;
    private BigDecimal cantidad;
    private String descripcion;
    private String foto;
    private Integer idProveedor;
}
