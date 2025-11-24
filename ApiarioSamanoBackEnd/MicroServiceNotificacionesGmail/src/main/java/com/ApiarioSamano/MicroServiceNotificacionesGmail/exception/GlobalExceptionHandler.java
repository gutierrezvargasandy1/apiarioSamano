package com.ApiarioSamano.MicroServiceNotificacionesGmail.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<Map<String, Object>> handleEmailException(EmailException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // Puedes ajustar según el tipo de excepción

        if (ex instanceof DestinatarioInvalidoException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (ex instanceof TemplateProcessingException) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", ex.getMessage());
        body.put("path", request.getRequestURI());

        return new ResponseEntity<>(body, status);
    }

    // Captura cualquier otra excepción inesperada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", ex.getMessage());
        body.put("path", request.getRequestURI());

        return new ResponseEntity<>(body, status);
    }
}