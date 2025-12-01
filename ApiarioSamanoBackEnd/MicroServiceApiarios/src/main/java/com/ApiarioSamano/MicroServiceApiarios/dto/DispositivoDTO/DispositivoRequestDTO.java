package com.ApiarioSamano.MicroServiceApiarios.dto.DispositivoDTO;

import java.util.List;

import lombok.Data;

@Data
public class DispositivoRequestDTO {
    private String dispositivoId;
    private String apiarioId;
    private String tipo;
    private List<String> sensores;
    private List<String> actuadores;
    private Long timestamp;
}