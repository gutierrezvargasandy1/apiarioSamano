package com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateriasPrimasRequest {
    private Long id;
    private String nombre;
    private String foto;
    private BigDecimal cantidad;
    private Long idAlmacen;
    private Integer idProvedor;
}
