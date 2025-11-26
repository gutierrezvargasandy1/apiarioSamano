package com.ApiarioSamano.MicroServiceApiarios.dto.OllamaDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Options {

    @JsonProperty("temperature")
    private Double temperature = 0.1;

    @JsonProperty("top_k")
    private Integer topK = 20;

    @JsonProperty("top_p")
    private Double topP = 0.9;

    @JsonProperty("num_predict")
    private Integer numPredict = 500;

    @JsonProperty("seed")
    private Integer seed;

    @JsonProperty("repeat_penalty")
    private Double repeatPenalty;
}