package com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HerramientasConProveedorResponse {
    private Long id;
    private String nombre;
    private String foto;
    private Long idAlmacen;
    private ProveedorResponseDTO proveedor;
}
