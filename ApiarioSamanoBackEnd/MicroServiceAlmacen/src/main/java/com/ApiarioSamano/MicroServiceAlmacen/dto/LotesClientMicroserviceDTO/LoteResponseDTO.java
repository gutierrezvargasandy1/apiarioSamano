package com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoteResponseDTO {

    private Long id;

    private String numeroSeguimiento;

    private String tipoProducto;

    private LocalDate fechaCreacion;

    private Long idAlmacen;

}
