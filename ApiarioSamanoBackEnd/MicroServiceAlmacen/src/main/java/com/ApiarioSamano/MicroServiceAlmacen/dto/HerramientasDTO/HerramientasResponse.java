package com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO;

import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HerramientasResponse {
    private Long id;
    private String nombre;
    private String foto;
    private Long idAlmacen;
    private Integer idProveedor;
}
