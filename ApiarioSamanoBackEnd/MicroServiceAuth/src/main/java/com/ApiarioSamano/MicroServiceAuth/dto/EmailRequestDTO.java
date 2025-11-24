package com.ApiarioSamano.MicroServiceAuth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDTO {
    private String destinatario;
    private String asunto;
    private Map<String, Object> variables;
}
