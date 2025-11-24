package com.ApiarioSamano.MicroServiceUsuario.controller;

import com.ApiarioSamano.MicroServiceUsuario.dto.ResponseDTO;
import com.ApiarioSamano.MicroServiceUsuario.dto.UsuarioRequestDTO;
import com.ApiarioSamano.MicroServiceUsuario.model.Usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Interfaz que define los endpoints REST para la gestión de usuarios.
 * 
 * Rutas base: {@code http://localhost:8081/api/usuarios}
 * (considerando que el servicio corre en el puerto 8081)
 */
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios")
public interface IUsuarioController {

    /**
     * Crear un nuevo usuario.
     * 
     * Método: POST
     * URL: {@code http://localhost:8081/api/usuarios}
     *
     * @param request DTO con los datos del usuario a crear
     * @return Usuario creado
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo usuario")
    ResponseEntity<ResponseDTO<Usuario>> crearUsuario(
            @RequestBody UsuarioRequestDTO request,
            @RequestHeader("Authorization") String jwt);

    /**
     * Obtener todos los usuarios.
     *
     * Método: GET
     * URL: {@code http://localhost:8081/api/usuarios}
     *
     * @return Lista de usuarios
     */
    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    ResponseEntity<ResponseDTO<List<Usuario>>> obtenerUsuarios();

    /**
     * Obtener un usuario por su ID.
     *
     * Método: GET
     * URL: {@code http://localhost:8081/api/usuarios/{id}}
     * Ejemplo: {@code http://localhost:8081/api/usuarios/1}
     *
     * @param id ID del usuario
     * @return Usuario encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por ID")
    ResponseEntity<ResponseDTO<Usuario>> obtenerUsuarioPorId(@PathVariable Long id);

    /**
     * Obtener un usuario por su correo electrónico.
     *
     * Método: GET
     * URL: {@code http://localhost:8081/api/usuarios/email/{email}}
     * Ejemplo: {@code http://localhost:8081/api/usuarios/email/test@correo.com}
     *
     * @param email Correo del usuario
     * @return Usuario encontrado
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener un usuario por Email")
    ResponseEntity<ResponseDTO<Usuario>> obtenerUsuarioPorEmail(@PathVariable String email);

    /**
     * Eliminar un usuario por ID.
     *
     * Método: DELETE
     * URL: {@code http://localhost:8081/api/usuarios/{id}}
     * Ejemplo: {@code http://localhost:8081/api/usuarios/1}
     *
     * @param id ID del usuario
     * @return Confirmación de eliminación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario por ID")
    ResponseEntity<ResponseDTO<Void>> eliminarUsuario(@PathVariable Long id);

    /**
     * Actualizar un usuario usando su correo electrónico.
     *
     * Método: PUT
     * URL: {@code http://localhost:8081/api/usuarios/email/{email}}
     * Ejemplo: {@code http://localhost:8081/api/usuarios/email/test@correo.com}
     *
     * @param email   Correo del usuario a actualizar
     * @param request DTO con los nuevos datos del usuario
     * @return Usuario actualizado
     */
    @PutMapping("/email/{email}")
    @Operation(summary = "Actualizar un usuario por Email")
    ResponseEntity<ResponseDTO<Usuario>> actualizarUsuarioPorEmail(
            @PathVariable String email,
            @RequestBody UsuarioRequestDTO request);
}
