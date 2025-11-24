package com.ApiarioSamano.MicroServiceUsuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequestDTO {
    private String nombre;
    private String apellidoPa;
    private String apellidoMa;
    private String email;
    private String contrasena;
    private String rol;
}