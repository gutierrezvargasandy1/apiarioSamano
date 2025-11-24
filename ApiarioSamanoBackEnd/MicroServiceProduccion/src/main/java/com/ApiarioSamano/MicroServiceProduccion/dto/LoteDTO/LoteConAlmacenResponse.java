package com.ApiarioSamano.MicroServiceProduccion.dto.LoteDTO;

import com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientAlmaceneDTO.AlmacenResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoteConAlmacenResponse {
    private Long id;
    private String numeroSeguimiento;
    private String tipoProducto;
    private LocalDate fechaCreacion;
    private Long idAlmacen;
    private AlmacenResponse almacen;
}
