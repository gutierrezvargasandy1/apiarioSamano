package com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO;

import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.AlmacenResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MateriasPrimasConProveedorDTO {
    private Long id;
    private String nombre;
    private String foto;
    private BigDecimal cantidad;
    private AlmacenResponse almacen;
    private ProveedorResponseDTO proveedor;
}