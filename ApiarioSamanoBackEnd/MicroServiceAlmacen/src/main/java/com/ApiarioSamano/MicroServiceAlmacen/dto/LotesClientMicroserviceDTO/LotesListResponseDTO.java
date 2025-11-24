package com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LotesListResponseDTO {
    private Integer estatus;
    private String descripcion;
    private List<LoteResponseDTO> data;
}