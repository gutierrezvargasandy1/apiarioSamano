package com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProveedorResponseDTO {
    private Long id;
    private String nombreEmpresa;
    private String nombreRepresentante;
    private String numTelefono;
    private String materialProvee;
    private byte[] fotografia;
}
