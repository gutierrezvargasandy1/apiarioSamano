package com.ApiarioSamano.MicroServiceProveedores.dto.ClientMicroServiceGestorArchivosDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArchivoDTO {
    private String id;
    private String nombreOriginal;
    private String nombreAlmacenado;
    private String rutaCompleta;
    private String tipoMime;
    private Long tamaño;
    private String servicioOrigen;
    private String entidadOrigen;
    private String entidadId;
    private String campoAsociado;
    private Integer ancho;
    private Integer alto;
    private String resolucion;
    private String hash;
    private java.time.LocalDateTime fechaSubida;
    private Boolean activo;

}
