package com.ApiarioSamano.MicroServiceNotificacionesGmail.controller;

import com.ApiarioSamano.MicroServiceNotificacionesGmail.dto.EmailRequestDTO;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

/**
 * Interfaz que define el contrato del controlador de envío de correos.
 */
@RequestMapping("/api/email")
public interface IEmailController {

    /**
     * Endpoint para enviar un correo HTML dinámico.
     * {
     * "destinatario": "androoz706@gmail.com",
     * "asunto": "23232d2d2d",
     * "variables": {
     * "nombreUsuario": "Androzz",
     * "codigoVerificacion": "23232d2d2d",
     * "fecha": "06/10/2025"
     * }
     * }
     * 
     *
     * @param request DTO con datos del correo (destinatario, asunto, variables para
     *                el template)
     * @return Mensaje de éxito o error
     */
    @PostMapping("/enviar")
    ResponseEntity<String> enviarCorreoConOtp(@RequestBody EmailRequestDTO request) throws MessagingException;

    @PostMapping("/enviarConAdjunto")
    ResponseEntity<String> enviarCorreoConContrasena(@RequestBody EmailRequestDTO request) throws MessagingException;
}
