package com.ApiarioSamano.MicroServiceGeneradorCodigo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ApiarioSamano.MicroServiceGeneradorCodigo.dto.CodigoResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Manejo de excepciones personalizadas del servicio
    @ExceptionHandler(CodigoException.class)
    public ResponseEntity<CodigoResponseDTO> handleCodigoException(CodigoException ex) {
        CodigoResponseDTO response = new CodigoResponseDTO(
                null,
                "ERROR",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Manejo general de errores inesperados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CodigoResponseDTO> handleGeneralException(Exception ex) {
        CodigoResponseDTO response = new CodigoResponseDTO(
                null,
                "ERROR",
                "Error interno del servidor: " + ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
