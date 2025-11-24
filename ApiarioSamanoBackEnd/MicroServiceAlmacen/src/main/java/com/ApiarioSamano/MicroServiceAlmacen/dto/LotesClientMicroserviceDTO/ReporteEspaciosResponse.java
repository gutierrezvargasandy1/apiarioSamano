package com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO;

import lombok.Data;
import java.util.List;

@Data
public class ReporteEspaciosResponse {
    private Long almacenId;
    private Integer capacidadTotal;
    private Integer espaciosInternos;
    private Integer materiasPrimas;
    private Integer herramientas;
    private Integer medicamentos;
    private Integer lotesExternos;
    private Integer totalEspaciosOcupados;
    private Integer espaciosDisponibles;
    private Double porcentajeOcupacion;
    private List<LoteResponseDTO> detalleLotes;
}