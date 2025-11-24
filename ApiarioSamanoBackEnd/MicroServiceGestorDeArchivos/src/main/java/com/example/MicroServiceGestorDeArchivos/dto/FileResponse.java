package com.example.MicroServiceGestorDeArchivos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    private String fileId;
    private String nombreOriginal;
    private String urlVisualizacion;
    private String tipoMime;
    private Long tamaño;
    private Integer ancho;
    private Integer alto;
}