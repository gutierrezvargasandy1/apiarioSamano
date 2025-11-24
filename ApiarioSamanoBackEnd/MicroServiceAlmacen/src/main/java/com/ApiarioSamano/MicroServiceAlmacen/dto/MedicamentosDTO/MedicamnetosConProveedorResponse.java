package com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO;

import java.math.BigDecimal;

import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicamnetosConProveedorResponse {
    private Long id;
    private String nombre;
    private BigDecimal cantidad;
    private String descripcion;
    private String foto;
    private ProveedorResponseDTO proveedor;
}
