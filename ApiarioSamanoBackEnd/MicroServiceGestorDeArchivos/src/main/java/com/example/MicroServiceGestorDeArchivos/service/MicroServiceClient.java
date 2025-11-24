package com.example.MicroServiceGestorDeArchivos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.MicroServiceGestorDeArchivos.config.JwtTokenProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MicroServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MicroServiceClient.class);

    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    // 🔹 URL base del microservicio generador (desde application.properties o .yml)
    @Value("${microservicio.generador.codigos.url}")
    private String generadorCodigoUrl;

    /**
     * 🔹 Crea headers con el JWT actual del JwtTokenProvider
     */
    private HttpHeaders createHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();

        if (contentType != null) {
            headers.setContentType(contentType);
        }

        String jwt = jwtTokenProvider.getCurrentJwtToken();
        if (jwt == null || jwt.isBlank()) {
            log.warn("⚠️ No se encontró un token JWT válido en JwtTokenProvider.");
        } else {
            headers.setBearerAuth(jwt);
        }

        return headers;
    }

    /**
     * 🔹 Genera un ID de archivo llamando al microservicio generador de códigos
     * enviando el JWT automáticamente en los headers.
     *
     * @return Código generado o null si ocurre error
     */
    public String generarIdArchivo() {
        String url = generadorCodigoUrl;
        log.info("📡 Solicitando ID de archivo a: {}", url);

        // 🔹 Crea la solicitud con el token en los headers
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders(null));

        try {
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Object.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() instanceof Map<?, ?> responseBody) {
                return (String) responseBody.get("codigo");
            } else {
                log.error("⚠️ Error al generar ID de archivo. Código HTTP: {}", response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Error al comunicarse con el microservicio generador de códigos: {}", e.getMessage());
            return null;
        }
    }
}
