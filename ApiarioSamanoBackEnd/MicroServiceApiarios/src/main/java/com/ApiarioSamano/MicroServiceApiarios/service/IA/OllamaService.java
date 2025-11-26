package com.ApiarioSamano.MicroServiceApiarios.service.IA;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ApiarioSamano.MicroServiceApiarios.dto.OllamaDTO.OllamaRequest;
import com.ApiarioSamano.MicroServiceApiarios.dto.OllamaDTO.OllamaResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.OllamaDTO.Options;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class OllamaService {

    private final WebClient ollamaWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ollama.model:gemma3:4b}")
    private String model;

    @Value("${ollama.url:http://ollama:11434}")
    private String ollamaUrl;

    public Map<String, Object> getOllamaStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            log.info("🔍 Verificando Ollama en: {}", ollamaUrl);

            String response = ollamaWebClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            boolean isRunning = response != null && !response.isEmpty();
            status.put("ollamaDisponible", isRunning);
            status.put("url", ollamaUrl);
            status.put("modeloConfigurado", model);
            status.put("respuestaCruda", response);

            if (isRunning) {
                boolean modeloDisponible = response.contains(model.replace(":", ""));
                status.put("modeloDisponible", modeloDisponible);
                status.put("mensaje", "✅ Ollama funcionando");

                if (!modeloDisponible) {
                    status.put("advertencia", "Modelo " + model + " no encontrado");
                    status.put("modelosDisponibles", response);
                }
            } else {
                status.put("mensaje", "❌ Ollama no responde");
            }

        } catch (Exception e) {
            log.error("❌ Error verificando Ollama: {}", e.getMessage());
            status.put("ollamaDisponible", false);
            status.put("error", e.getMessage());
            status.put("mensaje", "❌ Error de conexión con Ollama");
        }

        return status;
    }

    public boolean isOllamaRunning() {
        Map<String, Object> status = getOllamaStatus();
        return Boolean.TRUE.equals(status.get("ollamaDisponible"));
    }

    public String generateAnalysis(String systemPrompt, String userData, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("🚀 === INICIO generateAnalysis ===");

            Map<String, Object> status = getOllamaStatus();
            if (!Boolean.TRUE.equals(status.get("ollamaDisponible"))) {
                throw new RuntimeException("Ollama no disponible: " + status.get("mensaje"));
            }

            // Crear request
            OllamaRequest request = new OllamaRequest();
            request.setModel(model);
            request.setPrompt(userData);
            request.setSystem(systemPrompt);
            request.setStream(false);

            Options options = new Options();
            options.setTemperature(0.1);
            options.setNumPredict(500);
            request.setOptions(options);

            // Enviar request
            OllamaResponse response = ollamaWebClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(statusCode -> statusCode.isError(), clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(
                                        "HTTP " + clientResponse.statusCode() + ": " + errorBody)));
                    })
                    .bodyToMono(OllamaResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null) {
                throw new RuntimeException("Respuesta nula de Ollama - Timeout o conexión fallida");
            }

            // EXTRAER RESULTADO DEL MODELO
            String analysis = response.getResponse();

            if (analysis == null || analysis.trim().isEmpty()) {
                throw new RuntimeException("Ollama respondió vacío o sin texto");
            }

            long duration = System.currentTimeMillis() - startTime;

            log.info("🧠 Análisis completado en {} ms", duration);

            return analysis;

        } catch (Exception e) {

            long duration = System.currentTimeMillis() - startTime;

            log.error("💥 Error durante generateAnalysis después de {}ms: {}", duration, e.getMessage());

            throw new RuntimeException("Error en generateAnalysis: " + e.getMessage(), e);
        }
    }

    public String getModel() {
        return model;
    }

    public String getOllamaUrl() {
        return ollamaUrl;
    }
}
