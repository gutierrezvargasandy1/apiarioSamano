package com.ApiarioSamano.MicroServiceUsuario.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ApiarioSamano.MicroServiceUsuario.dto.CodigoResponseDTO;
import com.ApiarioSamano.MicroServiceUsuario.dto.EmailRequestDTO;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class MicroservicioClientService {

    private static final Logger log = LoggerFactory.getLogger(MicroservicioClientService.class);

    private final RestTemplate restTemplate;

    @Value("${microservicio.codigos.url}")
    private String urlGeneradorCodigos;

    @Value("${microservicio.email.url}")
    private String urlEnvioCorreo;

    public String generarContrasena(String jwt) {

        log.info("JWT generado para contraseña: {}", jwt);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.info("Enviando request a: {}", urlGeneradorCodigos);
        ResponseEntity<CodigoResponseDTO> response = restTemplate.exchange(
                urlGeneradorCodigos,
                HttpMethod.GET,
                entity,
                CodigoResponseDTO.class);

        log.info("Respuesta status: {}", response.getStatusCode());
        if (response.getBody() != null) {
            log.info("Respuesta body: {}", response.getBody());
        }

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                && "OK".equals(response.getBody().getEstatus())) {
            return response.getBody().getCodigo();
        } else {
            log.error("Error al generar contraseña: {}", response.getBody());
            throw new RuntimeException("Error al generar contraseña");
        }
    }

    public void enviarCorreo(String destinatario, String asunto, Map<String, Object> variables, String jwt) {
        log.info("JWT generado para envío de correo: {}", jwt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwt);

        EmailRequestDTO request = new EmailRequestDTO(destinatario, asunto, variables);
        HttpEntity<EmailRequestDTO> entity = new HttpEntity<>(request, headers);

        log.info("Enviando request a: {}", urlEnvioCorreo);
        ResponseEntity<String> response = restTemplate.exchange(
                urlEnvioCorreo,
                HttpMethod.POST,
                entity,
                String.class);

        log.info("Respuesta status: {}", response.getStatusCode());
        log.info("Respuesta body: {}", response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Error al enviar correo: {}", response.getBody());
            throw new RuntimeException("Error al enviar correo");
        }
    }
}
