package com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ApiarioSamano.MicroServiceAlmacen.config.JwtTokenProvider;
import com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO.LoteResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LotesClient {

    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Value("${microservicio.produccion.base-url}")
    private String baseUrl;

    public LotesClient(RestTemplate restTemplate, JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    private HttpHeaders createHeadersWithJwt(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);

        String jwt = jwtTokenProvider.getCurrentJwtToken();
        if (jwt != null) {
            headers.set("Authorization", "Bearer " + jwt);
            log.debug("JWT agregado a headers: {}", jwt);
        } else {
            log.warn("No se encontró JWT en el request actual");
        }

        return headers;
    }

    /**
     * Obtiene todos los lotes del microservicio de producción
     */
    public List<LoteResponseDTO> obtenerTodosLotes() {
        String url = baseUrl + "/api/lotes";
        log.info("🔄 Consultando todos los lotes del microservicio de producción: URL={}", url);

        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeadersWithJwt(MediaType.APPLICATION_JSON));

        try {
            log.debug("📡 Enviando request al microservicio de lotes...");

            // Intentar diferentes formatos de respuesta
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Object.class);
            log.info("✅ Request enviado. Código de estado HTTP: {}", response.getStatusCode());

            Object responseBody = response.getBody();
            if (responseBody == null) {
                log.error("❌ El microservicio de lotes devolvió un body nulo");
                return new ArrayList<>();
            }

            log.debug("📋 Respuesta cruda recibida: {}", responseBody);

            // Procesar la respuesta según el formato
            List<LoteResponseDTO> lotes = procesarRespuestaLotes(responseBody);
            log.info("✅ Lotes obtenidos correctamente. Cantidad: {}", lotes.size());

            if (!lotes.isEmpty()) {
                log.debug("📝 Detalle de lotes obtenidos:");
                lotes.forEach(lote -> log.debug("   - Lote ID: {}, Código: {}, Almacén: {}",
                        lote.getId(), lote.getNumeroSeguimiento(), lote.getIdAlmacen()));
            }

            return lotes;

        } catch (Exception e) {
            log.error("❌ Error al llamar al microservicio de lotes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener lotes del microservicio de producción: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa la respuesta del microservicio de lotes según diferentes formatos
     */
    @SuppressWarnings("unchecked")
    private List<LoteResponseDTO> procesarRespuestaLotes(Object responseBody) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(responseBody);
            log.debug("🔍 Procesando respuesta JSON: {}", jsonResponse);

            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // FORMATO 1: Respuesta con estructura CodigoResponse (con "codigo" en lugar de
            // "estatus")
            if (rootNode.has("codigo")) {
                JsonNode codigoNode = rootNode.get("codigo");
                if (!codigoNode.isNull()) {
                    int codigo = codigoNode.asInt();
                    if (codigo != 200) {
                        String descripcion = rootNode.has("descripcion") ? rootNode.get("descripcion").asText()
                                : "Sin descripción";
                        log.warn("⚠️ El microservicio devolvió un error: {} - {}", codigo, descripcion);
                        return new ArrayList<>();
                    }
                }
            }

            // Extraer la lista de lotes
            JsonNode dataNode = null;

            // Buscar el nodo de datos
            if (rootNode.has("data")) {
                dataNode = rootNode.get("data");
            } else if (rootNode.isArray()) {
                dataNode = rootNode; // La respuesta es directamente un array
            } else {
                log.warn("⚠️ Formato de respuesta no reconocido: {}", rootNode.getNodeType());
                return new ArrayList<>();
            }

            if (dataNode != null && dataNode.isArray()) {
                List<LoteResponseDTO> lotes = new ArrayList<>();
                for (JsonNode loteNode : dataNode) {
                    try {
                        LoteResponseDTO lote = objectMapper.treeToValue(loteNode, LoteResponseDTO.class);
                        if (lote != null) {
                            lotes.add(lote);
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Error al procesar un lote individual: {}", e.getMessage());
                        // Continuar con el siguiente lote
                    }
                }
                log.debug("✅ Procesados {} lotes correctamente", lotes.size());
                return lotes;
            }

            return new ArrayList<>();

        } catch (Exception e) {
            log.error("❌ Error al procesar respuesta del microservicio de lotes: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene lotes por ID de almacén
     */
    public List<LoteResponseDTO> obtenerLotesPorAlmacen(Long idAlmacen) {
        log.info("🔄 Consultando lotes del almacén ID: {} desde microservicio de producción", idAlmacen);

        // Primero obtenemos todos los lotes
        List<LoteResponseDTO> todosLotes = obtenerTodosLotes();

        // Luego filtramos por almacén
        List<LoteResponseDTO> lotesFiltrados = todosLotes.stream()
                .filter(lote -> lote.getIdAlmacen() != null && lote.getIdAlmacen().equals(idAlmacen))
                .collect(Collectors.toList());

        log.info("✅ Lotes del almacén {} obtenidos correctamente. Cantidad: {}", idAlmacen, lotesFiltrados.size());
        return lotesFiltrados;
    }

    /**
     * Obtiene un lote específico por ID
     */
    public LoteResponseDTO obtenerLotePorId(Long idLote) {
        String url = baseUrl + "/api/lotes/" + idLote;
        log.info("🔄 Consultando lote ID: {} desde microservicio de producción: URL={}", idLote, url);

        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeadersWithJwt(MediaType.APPLICATION_JSON));

        try {
            log.debug("📡 Enviando request al microservicio de lotes...");
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Object.class);
            log.info("✅ Request enviado. Código de estado HTTP: {}", response.getStatusCode());

            Object responseBody = response.getBody();
            if (responseBody == null) {
                log.error("❌ El microservicio de lotes devolvió un body nulo para lote {}", idLote);
                throw new IllegalStateException("El microservicio de lotes devolvió un body nulo para lote " + idLote);
            }

            // Procesar la respuesta individual
            List<LoteResponseDTO> lotes = procesarRespuestaLotes(responseBody);
            if (lotes.isEmpty()) {
                log.error("❌ No se pudo procesar el lote con ID {}", idLote);
                throw new IllegalStateException("No se pudo procesar el lote con ID " + idLote);
            }

            LoteResponseDTO lote = lotes.get(0);
            log.info("✅ Lote obtenido correctamente. ID: {}, Código: {}", lote.getId(), lote.getNumeroSeguimiento());
            return lote;

        } catch (Exception e) {
            log.error("❌ Error al llamar al microservicio de lotes para lote {}: {}", idLote, e.getMessage(), e);
            throw new RuntimeException("Error al obtener lote " + idLote + ": " + e.getMessage(), e);
        }
    }
}