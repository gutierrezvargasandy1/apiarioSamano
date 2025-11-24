package com.ApiarioSamano.MicroServiceAuth.controller;

import com.ApiarioSamano.MicroServiceAuth.dto.AuthResponse;
import com.ApiarioSamano.MicroServiceAuth.dto.LoginRequest;
import com.ApiarioSamano.MicroServiceAuth.dto.ResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interfaz que define los endpoints REST para el microservicio de
 * autenticación.
 * 
 * Rutas base: {@code http://localhost:8082/api/auth}
 */
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Operaciones relacionadas con login y recuperación de contraseña")
public interface IAuthController {

    /**
     * Iniciar sesión con credenciales de usuario.
     *
     * Método: POST
     * URL: {@code http://localhost:8082/api/auth/login}
     * Ejemplo Body:
     * {
     * "email": "usuario@correo.com",
     * "contrasena": "123456"
     * }
     *
     * @param request DTO con las credenciales
     * @return Token JWT y datos básicos de autenticación
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con credenciales y obtener token JWT")
    ResponseEntity<ResponseDTO<AuthResponse>> login(@Valid @RequestBody LoginRequest request);

    /**
     * Inicia la recuperación de contraseña enviando un OTP al correo del usuario.
     *
     * Método: POST
     * URL: {@code http://localhost:8082/api/auth/recuperar}
     * Ejemplo Body:
     * {
     * "email": "usuario@correo.com"
     * }
     *
     * @param request Mapa con el correo del usuario
     * @return Mensaje indicando que el OTP fue enviado
     */
    @PostMapping("/recuperar")
    @Operation(summary = "Enviar OTP al correo del usuario para recuperación de contraseña")
    ResponseEntity<ResponseDTO<String>> iniciarRecuperacion(@RequestBody Map<String, String> request);

    /**
     * Verifica el OTP recibido y permite cambiar la contraseña.
     *
     * Método: POST
     * URL: {@code http://localhost:8082/api/auth/recuperar/verificar}
     * Ejemplo Body:
     * {
     * "email": "usuario@correo.com",
     * "otp": "123456",
     * "nuevaContrasena": "nuevaClave123"
     * }
     *
     * @param request Mapa con email, OTP y nueva contraseña
     * @return Mensaje indicando que la contraseña fue cambiada
     */
    @PostMapping("/recuperar/verificar")
    @Operation(summary = "Verificar OTP y actualizar la contraseña del usuario")
    ResponseEntity<ResponseDTO<String>> verificarOtpYCambiarContrasena(@RequestBody Map<String, String> request);

    @PostMapping("/recuperar/cambiar")
    @Operation(summary = "Cambiar la contraseña del usuario")
    ResponseEntity<ResponseDTO<String>> cambiarContrasena(@RequestBody Map<String, String> request);

    @PostMapping("/recuperar/cambiar-temporal")
    @Operation(summary = "Cambiar contraseña usando la contraseña temporal")
    ResponseEntity<ResponseDTO<String>> cambiarContrasenaTemporal(@RequestBody Map<String, String> request);
}
