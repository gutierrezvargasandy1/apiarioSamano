package com.ApiarioSamano.MicroServiceApiarios.service.MicroServicesAPI;

import com.ApiarioSamano.MicroServiceApiarios.config.JwtTokenProvider;
import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
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
public class MicroServiceClientMedicamentos {

    private static final Logger log = LoggerFactory.getLogger(MicroServiceClientMedicamentos.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${microservice.almacen.url}")
    private String urlAlmacen;

    public List<MedicamentosResponse> obtenerTodos() {
        log.info("Iniciando obtención de todos los medicamentos...");

        String token = jwtTokenProvider.getCurrentJwtToken();
        log.debug("Token JWT obtenido: {}", token != null ? token : "[NO HAY TOKEN]");
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
            String endpoint = urlAlmacen + "/todos";
            log.info("Realizando llamada HTTP GET a {}", endpoint);

            ResponseEntity<CodigoResponse<MedicamentosResponse[]>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<CodigoResponse<MedicamentosResponse[]>>() {
                    });

            CodigoResponse<MedicamentosResponse[]> codigoResponse = response.getBody();
            log.debug("Respuesta recibida: {}", codigoResponse);

            if (codigoResponse != null && codigoResponse.getData() != null) {
                log.info("Medicamentos obtenidos correctamente. Cantidad: {}", codigoResponse.getData().length);
                return Arrays.asList(codigoResponse.getData());
            } else {
                log.warn("No se encontraron medicamentos en la respuesta del microservicio.");
                return List.of();
            }

        } catch (Exception e) {
            log.error("Error al obtener medicamentos desde el microservicio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener medicamentos desde el microservicio", e);
        }
    }

    // 📌 NUEVO MÉTODO: OBTENER MEDICAMENTO POR ID
    public MedicamentosResponse obtenerPorId(Long id) {
        log.info("Iniciando obtención del medicamento con ID: {}", id);

        String token = jwtTokenProvider.getCurrentJwtToken();
        log.debug("Token JWT obtenido: {}", token != null ? token : "[NO HAY TOKEN]");
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

            ResponseEntity<CodigoResponse<MedicamentosResponse>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<CodigoResponse<MedicamentosResponse>>() {
                    });

            CodigoResponse<MedicamentosResponse> codigoResponse = response.getBody();
            log.debug("Respuesta recibida: {}", codigoResponse);

            if (codigoResponse != null && codigoResponse.getData() != null) {
                log.info("Medicamento obtenido correctamente: {}", codigoResponse.getData().getNombre());
                return codigoResponse.getData();
            } else {
                log.warn("No se encontró el medicamento con ID: {} en la respuesta del microservicio.", id);
                throw new RuntimeException("Medicamento no encontrado con ID: " + id);
            }

        } catch (Exception e) {
            log.error("Error al obtener medicamento con ID {} desde el microservicio: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al obtener medicamento desde el microservicio", e);
        }
    }
}
