package com.ApiarioSamano.MicroServiceUsuario.controller;

import com.ApiarioSamano.MicroServiceUsuario.dto.ResponseDTO;
import com.ApiarioSamano.MicroServiceUsuario.dto.UsuarioRequestDTO;
import com.ApiarioSamano.MicroServiceUsuario.model.Usuario;
import com.ApiarioSamano.MicroServiceUsuario.services.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios.
 * Expone endpoints para operaciones CRUD sobre la entidad Usuario.
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios")
public class UsuarioController {

        private final UsuarioService usuarioService;

        public UsuarioController(UsuarioService usuarioService) {
                this.usuarioService = usuarioService;
        }

        @PostMapping
        @Operation(summary = "Crear un nuevo usuario")
        public ResponseEntity<ResponseDTO<Usuario>> crearUsuario(
                        @RequestBody UsuarioRequestDTO request,
                        @RequestHeader("Authorization") String jwt) { // Se asume que JWT viene en header
                                                                      // "Authorization"

                Usuario nuevoUsuario = Usuario.builder()
                                .nombre(request.getNombre())
                                .apellidoPa(request.getApellidoPa())
                                .apellidoMa(request.getApellidoMa())
                                .email(request.getEmail())
                                .rol(request.getRol())
                                .build();

                Usuario guardado = usuarioService.guardarUsuario(nuevoUsuario, jwt);

                ResponseDTO<Usuario> response = ResponseDTO.<Usuario>builder()
                                .statusCode(HttpStatus.CREATED.value())
                                .message("Usuario creado correctamente")
                                .data(guardado)
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping
        @Operation(summary = "Obtener todos los usuarios")
        public ResponseEntity<ResponseDTO<List<Usuario>>> obtenerUsuarios() {
                List<Usuario> usuarios = usuarioService.obtenerUsuarios();

                ResponseDTO<List<Usuario>> response = ResponseDTO.<List<Usuario>>builder()
                                .statusCode(HttpStatus.OK.value())
                                .message("Usuarios obtenidos correctamente")
                                .data(usuarios)
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Obtener un usuario por su ID")
        public ResponseEntity<ResponseDTO<Usuario>> obtenerUsuarioPorId(@PathVariable Long id) {
                Usuario usuario = usuarioService.obtenerPorId(id);

                ResponseDTO<Usuario> response = ResponseDTO.<Usuario>builder()
                                .statusCode(HttpStatus.OK.value())
                                .message("Usuario encontrado")
                                .data(usuario)
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/email/{email}")
        @Operation(summary = "Obtener un usuario por su correo electrónico")
        public ResponseEntity<ResponseDTO<Usuario>> obtenerUsuarioPorEmail(@PathVariable String email) {
                Usuario usuario = usuarioService.obtenerPorEmail(email);

                ResponseDTO<Usuario> response = ResponseDTO.<Usuario>builder()
                                .statusCode(HttpStatus.OK.value())
                                .message("Usuario encontrado")
                                .data(usuario)
                                .build();

                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar un usuario por su ID")
        public ResponseEntity<ResponseDTO<Void>> eliminarUsuario(@PathVariable Long id) {
                usuarioService.eliminarUsuario(id);

                ResponseDTO<Void> response = ResponseDTO.<Void>builder()
                                .statusCode(HttpStatus.OK.value())
                                .message("Usuario eliminado correctamente")
                                .build();

                return ResponseEntity.ok(response);
        }

        @PutMapping("/email/{email}")
        @Operation(summary = "Actualizar un usuario a partir de su correo electrónico")
        public ResponseEntity<ResponseDTO<Usuario>> actualizarUsuarioPorEmail(
                        @PathVariable String email,
                        @RequestBody UsuarioRequestDTO request) {

                Usuario usuarioActualizado = Usuario.builder()
                                .nombre(request.getNombre())
                                .apellidoPa(request.getApellidoPa())
                                .apellidoMa(request.getApellidoMa())
                                .email(request.getEmail())
                                .contrasena(request.getContrasena())
                                .rol(request.getRol())
                                .build();

                Usuario actualizado = usuarioService.actualizarPorEmail(email, usuarioActualizado);

                ResponseDTO<Usuario> response = ResponseDTO.<Usuario>builder()
                                .statusCode(HttpStatus.OK.value())
                                .message("Usuario actualizado correctamente")
                                .data(actualizado)
                                .build();

                return ResponseEntity.ok(response);
        }
}
