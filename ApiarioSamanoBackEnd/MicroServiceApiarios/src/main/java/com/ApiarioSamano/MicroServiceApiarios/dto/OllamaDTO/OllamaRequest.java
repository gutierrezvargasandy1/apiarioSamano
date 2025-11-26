package com.ApiarioSamano.MicroServiceApiarios.dto.OllamaDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OllamaRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("stream")
    private Boolean stream = false;

    @JsonProperty("system")
    private String system;

    @JsonProperty("context")
    private String context;

    @JsonProperty("options")
    private Options options;

    // Constructor conveniente
    public OllamaRequest(String model, String prompt) {
        this.model = model;
        this.prompt = prompt;
        this.stream = false;
    }
}