package com.example.MicroServiceGestorDeArchivos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "archivos")
public class Archivo {
    @Id
    private String id;

    @Column(nullable = false)
    private String nombreOriginal;

    @Column(nullable = false)
    private String nombreAlmacenado;

    @Column(nullable = false)
    private String rutaCompleta;

    @Column(nullable = false)
    private String tipoMime;

    private Long tamaño;

    // Metadata para imágenes
    private Integer ancho;
    private Integer alto;
    private String resolucion;

    private String hash;

    @CreationTimestamp
    private LocalDateTime fechaSubida;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activo = true;
}