package com.example.MicroServiceGestorDeArchivos.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponse {
    private String fileId;
    private String nombreOriginal;
    private String urlDescarga;
    private String urlVisualizacion;
    private Long tamaño;
    private String tipoMime;
    private Integer ancho;
    private Integer alto;
    private LocalDateTime fechaSubida;
}
