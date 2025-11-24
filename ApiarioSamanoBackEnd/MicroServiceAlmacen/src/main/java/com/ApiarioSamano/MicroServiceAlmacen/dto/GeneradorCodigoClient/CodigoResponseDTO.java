package com.ApiarioSamano.MicroServiceAlmacen.dto.GeneradorCodigoClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CodigoResponseDTO {

    private String codigo;
    private String estatus;
    private String descripcion;

}