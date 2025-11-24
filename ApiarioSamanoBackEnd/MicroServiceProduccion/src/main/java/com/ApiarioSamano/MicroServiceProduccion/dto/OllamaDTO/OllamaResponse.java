package com.ApiarioSamano.MicroServiceProduccion.dto.OllamaDTO;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 🔹 IMPORTANTE: ignora campos desconocidos
public class OllamaResponse {
    private String model;
    private String response;
    private Boolean done;
    private String[] context;
    private Long totalDuration;
    private Long loadDuration;
    private Integer promptEvalCount;
    private Integer evalCount;

    // 🔹 Método de utilidad
    public boolean isSuccess() {
        return response != null && !response.trim().isEmpty();
    }
}