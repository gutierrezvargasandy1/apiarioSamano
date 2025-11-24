package com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI;

import com.ApiarioSamano.MicroServiceAlmacen.config.JwtTokenProvider;
import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedoresClientMicroservice {

    private static final Logger log = LoggerFactory.getLogger(ProveedoresClientMicroservice.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${microservice.proveedores.url}")
    private String urlProveedores;

    public List<ProveedorResponseDTO> obtenerTodosProveedores() {
        log.info("Iniciando obtención de todos los proveedores...");

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
            log.info("Realizando llamada HTTP GET a {}", urlProveedores);
            ResponseEntity<CodigoResponse<ProveedorResponseDTO[]>> response = restTemplate.exchange(
                    urlProveedores,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<CodigoResponse<ProveedorResponseDTO[]>>() {
                    });

            CodigoResponse<ProveedorResponseDTO[]> codigoResponse = response.getBody();
            log.debug("Respuesta recibida: {}", codigoResponse);

            if (codigoResponse != null && codigoResponse.getData() != null) {
                log.info("Proveedores obtenidos correctamente. Cantidad: {}", codigoResponse.getData().length);
                return Arrays.asList(codigoResponse.getData());
            } else {
                log.warn("No se encontraron proveedores en la respuesta del microservicio.");
                return List.of();
            }

        } catch (Exception e) {
            log.error("Error al obtener proveedores desde el microservicio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener proveedores desde el microservicio", e);
        }
    }
}
