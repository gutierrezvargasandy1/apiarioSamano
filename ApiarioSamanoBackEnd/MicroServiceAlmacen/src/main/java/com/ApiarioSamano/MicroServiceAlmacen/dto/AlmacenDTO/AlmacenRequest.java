package com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlmacenRequest {
    private String ubicacion;
    private Integer capacidad;

}
