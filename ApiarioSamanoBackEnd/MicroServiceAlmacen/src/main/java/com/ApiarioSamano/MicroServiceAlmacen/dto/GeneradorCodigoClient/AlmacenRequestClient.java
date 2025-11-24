package com.ApiarioSamano.MicroServiceAlmacen.dto.GeneradorCodigoClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlmacenRequestClient {
    private String zona;
    private String producto;

}