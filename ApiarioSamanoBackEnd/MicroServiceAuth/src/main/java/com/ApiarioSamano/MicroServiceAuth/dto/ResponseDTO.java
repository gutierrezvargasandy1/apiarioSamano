package com.ApiarioSamano.MicroServiceAuth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {
    private int statusCode;
    private String message;
    private String description;
    private T data;

    public static <T> ResponseDTO<T> success(T data, String message) {
        return ResponseDTO.<T>builder()
                .statusCode(200)
                .message(message)
                .description("Operación exitosa")
                .data(data)
                .build();
    }

    public static <T> ResponseDTO<T> created(T data, String message) {
        return ResponseDTO.<T>builder()
                .statusCode(201)
                .message(message)
                .description("Recurso creado exitosamente")
                .data(data)
                .build();
    }
}