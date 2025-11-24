package com.ApiarioSamano.MicroServiceProduccion.services.MicroServiceClientGeneradorCodigo;

import com.ApiarioSamano.MicroServiceProduccion.config.JwtTokenProvider;
import com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientGeneradorCoigoDTO.CodigoResponseDTO;
import com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientGeneradorCoigoDTO.LoteRequestClient;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CodigoClientMicroservice {

    private static final Logger log = LoggerFactory.getLogger(CodigoClientMicroservice.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${microservice.generadorcodigo.url}")
    private String urlGeneradorCodigo;

    /**
     * Llama al microservicio de códigos para generar un código de lote.
     *
     * @param request LoteRequest con los datos necesarios
     * @return CodigoResponseDTO con el código generado
     */
    public String generarLote(LoteRequestClient request) {
        log.info("Generando código de lote para: {}", request);

        String token = jwtTokenProvider.getCurrentJwtToken();
        if (token == null) {
            log.error("No se encontró token JWT válido");
            throw new RuntimeException("No se encontró un token JWT válido en la solicitud actual.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoteRequestClient> entity = new HttpEntity<>(request, headers);

        try {
            String endpoint = urlGeneradorCodigo + "/lote";
            log.info("Llamando al endpoint {}", endpoint);

            ResponseEntity<CodigoResponseDTO> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<CodigoResponseDTO>() {}
            );

            CodigoResponseDTO codigoResponse = response.getBody();
            String res = response.getBody().getCodigo();
            log.debug("Respuesta recibida: {}", codigoResponse);

            return res;

        } catch (Exception e) {
            log.error("Error al generar código de lote: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar código de lote", e);
        }
    }
}
