package com.ApiarioSamano.MicroServiceApiarios.dto.OllamaDTO;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Options {
    private Double temperature = 0.1;

    @JsonProperty("top_k")
    private Integer topK = 20;

    @JsonProperty("top_p")
    private Double topP = 0.9;

    @JsonProperty("num_predict")
    private Integer numPredict = 500; // 🔹 Nombre correcto

    private Integer seed;
    private Double repeatPenalty;

    public Integer getNumPredict() {
        return numPredict;
    }

    public void setNumPredict(Integer numPredict) {
        this.numPredict = numPredict;
    }

    public void setNum_predict(Integer numPredict) {
        this.numPredict = numPredict;
    }
}