package com.ApiarioSamano.MicroServiceNotificacionesGmail.controller;

import com.ApiarioSamano.MicroServiceNotificacionesGmail.dto.EmailRequestDTO;
import com.ApiarioSamano.MicroServiceNotificacionesGmail.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController implements IEmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    @PostMapping("/enviarOtp")
    public ResponseEntity<String> enviarCorreoConOtp(@RequestBody EmailRequestDTO request) {
        emailService.enviarCorreoConOtp(request);
        return ResponseEntity.ok("Correo enviado correctamente a " + request.getDestinatario());
    }

    @Override
    @PostMapping("/enviarContrasena")
    public ResponseEntity<String> enviarCorreoConContrasena(@RequestBody EmailRequestDTO request) {
        emailService.enviarCorreoConContrasenaTemporal(request);
        return ResponseEntity.ok("Correo enviado correctamente a " + request.getDestinatario());
    }
}
