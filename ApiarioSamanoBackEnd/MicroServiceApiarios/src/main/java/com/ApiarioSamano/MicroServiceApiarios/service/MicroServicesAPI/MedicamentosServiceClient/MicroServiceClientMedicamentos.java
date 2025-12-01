package com.ApiarioSamano.MicroServiceApiarios.service.MicroServicesAPI.MedicamentosServiceClient;

import com.ApiarioSamano.MicroServiceApiarios.config.JwtTokenProvider;
import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
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
public class MicroServiceClientMedicamentos implements IMedicamentosService {

    private static final Logger log = LoggerFactory.getLogger(MicroServiceClientMedicamentos.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    @Value("${microservice.almacen.url}")
    private String urlAlmacen;

    // CORRECCIÓN: Usar RestTemplate inyectado en lugar de crear uno nuevo
    public MicroServiceClientMedicamentos(JwtTokenProvider jwtTokenProvider, RestTemplate restTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<MedicamentosResponse> obtenerTodos() {
        log.info("🔄 [MEDICAMENTOS] Iniciando obtención de todos los medicamentos...");

        String token = jwtTokenProvider.getCurrentJwtToken();
        log.debug("🔐 [MEDICAMENTOS] Token JWT obtenido: {}", token != null ? token : "[NO HAY TOKEN]");
        if (token == null) {
            log.error("❌ [MEDICAMENTOS] No se encontró un token JWT válido en la solicitud actual.");
            throw new RuntimeException("No se encontró un token JWT válido en la solicitud actual.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.debug("📋 [MEDICAMENTOS] Headers preparados: {}", headers);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            String endpoint = urlAlmacen + "/todos";
            log.info("🌐 [MEDICAMENTOS] Realizando llamada HTTP GET a {}", endpoint);

            ResponseEntity<CodigoResponse<MedicamentosResponse[]>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<CodigoResponse<MedicamentosResponse[]>>() {
                    });

            CodigoResponse<MedicamentosResponse[]> codigoResponse = response.getBody();
            log.debug("📨 [MEDICAMENTOS] Respuesta recibida: {}", codigoResponse);

            if (codigoResponse != null && codigoResponse.getData() != null) {
                log.info("✅ [MEDICAMENTOS] Medicamentos obtenidos correctamente. Cantidad: {}",
                        codigoResponse.getData().length);
                return Arrays.asList(codigoResponse.getData());
            } else {
                log.warn("⚠️ [MEDICAMENTOS] No se encontraron medicamentos en la respuesta del microservicio.");
                return List.of();
            }

        } catch (Exception e) {
            log.error("❌ [MEDICAMENTOS] Error al obtener medicamentos desde el microservicio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener medicamentos desde el microservicio", e);
        }
    }

    @Override
    public MedicamentosResponse obtenerPorId(Long id) {
        log.info("🔄 [MEDICAMENTOS] Iniciando obtención del medicamento con ID: {}", id);

        String token = jwtTokenProvider.getCurrentJwtToken();
        log.debug("🔐 [MEDICAMENTOS] Token JWT obtenido: {}", token != null ? token : "[NO HAY TOKEN]");
        if (token == null) {
            log.error("❌ [MEDICAMENTOS] No se encontró un token JWT válido en la solicitud actual.");
            throw new RuntimeException("No se encontró un token JWT válido en la solicitud actual.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.debug("📋 [MEDICAMENTOS] Headers preparados: {}", headers);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            String endpoint = urlAlmacen + "/" + id;
            log.info("🌐 [MEDICAMENTOS] Realizando llamada HTTP GET a {}", endpoint);

            ResponseEntity<CodigoResponse<MedicamentosResponse>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<CodigoResponse<MedicamentosResponse>>() {
                    });

            CodigoResponse<MedicamentosResponse> codigoResponse = response.getBody();
            log.debug("📨 [MEDICAMENTOS] Respuesta recibida: {}", codigoResponse);

            if (codigoResponse != null && codigoResponse.getData() != null) {
                log.info("✅ [MEDICAMENTOS] Medicamento obtenido correctamente: {}",
                        codigoResponse.getData().getNombre());
                return codigoResponse.getData();
            } else {
                log.warn(
                        "⚠️ [MEDICAMENTOS] No se encontró el medicamento con ID: {} en la respuesta del microservicio.",
                        id);
                throw new RuntimeException("Medicamento no encontrado con ID: " + id);
            }

        } catch (Exception e) {
            log.error("❌ [MEDICAMENTOS] Error al obtener medicamento con ID {} desde el microservicio: {}", id,
                    e.getMessage(), e);
            throw new RuntimeException("Error al obtener medicamento desde el microservicio", e);
        }
    }
}