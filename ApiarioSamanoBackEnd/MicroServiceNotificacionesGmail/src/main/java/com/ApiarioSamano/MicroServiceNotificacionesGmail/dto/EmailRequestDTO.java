package com.ApiarioSamano.MicroServiceNotificacionesGmail.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDTO {
    private String destinatario;
    private String asunto;
    private Map<String, Object> variables;
}