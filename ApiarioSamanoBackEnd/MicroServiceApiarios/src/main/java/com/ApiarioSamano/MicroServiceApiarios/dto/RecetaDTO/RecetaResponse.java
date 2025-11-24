package com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecetaResponse {
    private Long id;
    private Long idRecetaPadre;
    private String descripcion;
    private LocalDateTime fechaDeCreacion;
}