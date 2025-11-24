package com.ApiarioSamano.MicroServiceUsuario.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🔹 Usuario no encontrado
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioNotFound(UsuarioNotFoundException ex) {
        return buildResponse("Usuario no encontrado", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 🔹 Usuario ya existe (duplicado)
    @ExceptionHandler(UsuarioAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioExists(UsuarioAlreadyExistsException ex) {
        return buildResponse("Usuario duplicado", ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 🔹 Validaciones de campos (Bean Validation @NotNull, @Email, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", "Error de validación");
        body.put("status", HttpStatus.BAD_REQUEST.value());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        body.put("fields", fieldErrors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 🔹 Error al parsear JSON (cuando llega un request mal formado)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(HttpMessageNotReadableException ex) {
        return buildResponse("JSON inválido", "Error en el formato de la petición", HttpStatus.BAD_REQUEST);
    }

    // 🔹 Violación de restricciones en BD (ej: unique constraint)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return buildResponse("Violación de integridad de datos",
                "Los datos no cumplen restricciones en la base de datos", HttpStatus.CONFLICT);
    }

    // 🔹 Entidad no encontrada en consultas JPA
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponse("Entidad no encontrada", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 🔹 Acceso denegado (cuando uses seguridad con Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse("Acceso denegado", "No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN);
    }

    // 🔹 Errores genéricos
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse("Error interno", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Método auxiliar para armar respuestas
    private ResponseEntity<Map<String, Object>> buildResponse(String error, String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", error);
        body.put("message", message);
        body.put("status", status.value());
        return new ResponseEntity<>(body, status);
    }
}
