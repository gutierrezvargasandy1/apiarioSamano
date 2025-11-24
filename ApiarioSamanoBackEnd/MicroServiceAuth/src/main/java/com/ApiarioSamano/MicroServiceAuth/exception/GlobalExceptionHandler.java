package com.ApiarioSamano.MicroServiceAuth.exception;

import com.ApiarioSamano.MicroServiceAuth.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja nuestras excepciones personalizadas
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ResponseDTO<Object>> handleAuthException(AuthException ex) {
        ResponseDTO<Object> response = ResponseDTO.builder()
                .statusCode(ex.getStatusCode())
                .message(ex.getMessage())
                .description(ex.getDescription())
                .data(null)
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    // Maneja errores de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ResponseDTO<Map<String, String>> response = ResponseDTO.<Map<String, String>>builder()
                .statusCode(400)
                .message("Error de validación")
                .description("Los datos de entrada no son válidos")
                .data(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // Maneja excepciones genéricas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Object>> handleGenericException(Exception ex) {
        ResponseDTO<Object> response = ResponseDTO.builder()
                .statusCode(500)
                .message("Error interno del servidor")
                .description("Ocurrió un error inesperado")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}