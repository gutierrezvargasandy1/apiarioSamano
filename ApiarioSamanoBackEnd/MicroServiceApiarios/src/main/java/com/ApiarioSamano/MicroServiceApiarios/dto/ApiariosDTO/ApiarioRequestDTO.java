package com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiarioRequestDTO {
    private Integer numeroApiario;
    private String ubicacion;
    private String salud;

}
