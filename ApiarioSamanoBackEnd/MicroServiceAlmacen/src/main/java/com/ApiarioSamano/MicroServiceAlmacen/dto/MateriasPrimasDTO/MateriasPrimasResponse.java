package com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO;

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
    private String foto;
    private BigDecimal cantidad;
    private Integer idProveedor;
}
