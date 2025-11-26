package com.ApiarioSamano.MicroServiceApiarios.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dispositivo {
    private String dispositivoId;
    private String apiarioId;
    private String tipo;
    private java.util.List<String> sensores;
    private java.util.List<String> actuadores;
    private Long timestamp;
}