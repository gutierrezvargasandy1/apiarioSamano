package com.ApiarioSamano.MicroServiceProduccion.services.MicroServiceClientAlmacen;


import com.ApiarioSamano.MicroServiceProduccion.config.JwtTokenProvider;
import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientAlmaceneDTO.AlmacenResponse;

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
public class AlmacenClientMicroservice {

    private static final Logger log = LoggerFactory.getLogger(AlmacenClientMicroservice.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();

    // 🔹 Se obtiene la URL desde application.properties
    @Value("${microservice.almacen.url}")
    private String urlAlmacen;

    /**
     * Obtiene un almacén por ID desde el microservicio de Almacén.
     * @param id identificador del almacén
     * @return CodigoResponse<AlmacenDTO> con la información del almacén
     */
    public CodigoResponse<AlmacenResponse> obtenerAlmacenPorId(Long id) {
        log.info("Iniciando obtención de almacén con ID: {}", id);

        String token = jwtTokenProvider.getCurrentJwtToken();
        log.debug("Token JWT obtenido: {}", token);

        if (token == null) {
            log.error("No se encontró un token JWT válido en la solicitud actual.");
            throw new RuntimeException("No se encontró un token JWT válido en la solicitud actual.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.debug("Headers preparados: {}", headers);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            String endpoint = urlAlmacen + "/" + id;
            log.info("Realizando llamada HTTP GET a {}", endpoint);

            ResponseEntity<CodigoResponse<AlmacenResponse>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<CodigoResponse<AlmacenResponse>>() {
                    });

            CodigoResponse<AlmacenResponse> codigoResponse = response.getBody();
            log.debug("Respuesta recibida: {}", codigoResponse);

            if (codigoResponse != null && codigoResponse.getData() != null) {
                log.info("Almacén obtenido correctamente: {}", codigoResponse.getData());
                return codigoResponse;
            } else {
                log.warn("No se encontró información de almacén en la respuesta.");
                return new CodigoResponse<>(404, "Almacén no encontrado", null);
            }

        } catch (Exception e) {
            log.error("Error al obtener almacén desde el microservicio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener almacén desde el microservicio", e);
        }
    }
}
