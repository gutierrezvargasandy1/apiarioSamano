package com.ApiarioSamano.MicroServiceApiarios.dto.OllamaDTO;

import lombok.Data;

@Data
public class OllamaRequest {
    private String model;
    private String prompt;
    private Boolean stream = false;
    private String system; // 🔹 NUEVO: para system prompt separado
    private String context;
    private Options options;

    // 🔹 Constructor para fácil uso
    public OllamaRequest() {
    }

    public OllamaRequest(String model, String prompt) {
        this.model = model;
        this.prompt = prompt;
        this.stream = false;
    }
}