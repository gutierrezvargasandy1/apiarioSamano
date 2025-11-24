package com.ApiarioSamano.MicroServiceApiarios.service.IA;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.ApiarioSamano.MicroServiceApiarios.dto.OllamaDTO.OllamaRequest;
import com.ApiarioSamano.MicroServiceApiarios.dto.OllamaDTO.OllamaResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.OllamaDTO.Options;

import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Log4j2
public class OllamaService {

    private final WebClient ollamaWebClient;

    @Value("${ollama.model}")
    private String model;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.timeout.default:30}")
    private int defaultTimeout;

    public boolean isOllamaRunning() {
        try {
            log.debug("🔍 Verificando disponibilidad de Ollama...");

            String response = ollamaWebClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            boolean isRunning = response != null && !response.isEmpty() &&
                    (response.contains("models") || response.contains("\"name\""));

            log.debug("✅ Ollama estado: {}", isRunning ? "DISPONIBLE" : "NO DISPONIBLE");
            return isRunning;

        } catch (Exception e) {
            log.debug("❌ Ollama no disponible: {}", e.getMessage());
            return false;
        }
    }

    public String generateAnalysis(String systemPrompt, String userData, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("🚀 Iniciando análisis con modelo: {} (timeout: {}s)", model, timeoutSeconds);

            String datosOptimizados = optimizarDatosEntrada(userData);
            String systemOptimizado = optimizarSystemPrompt(systemPrompt);

            log.debug("📝 Datos: {} chars | System: {} chars",
                    datosOptimizados.length(), systemOptimizado.length());

            OllamaRequest request = new OllamaRequest();
            request.setModel(model);
            request.setPrompt(datosOptimizados);
            request.setSystem(systemOptimizado);
            request.setStream(false);

            Options options = new Options();
            options.setTemperature(0.3);
            options.setTopK(20);
            options.setTopP(0.8);
            options.setNumPredict(300);
            options.setRepeatPenalty(1.1);
            request.setOptions(options);

            log.info("📤 Enviando request a Ollama: {}/api/generate", ollamaUrl);

            OllamaResponse response = ollamaWebClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(request), OllamaRequest.class)
                    .retrieve()
                    .bodyToMono(OllamaResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .doOnError(error -> log.error("❌ Error en llamada a Ollama: {} - {}",
                            error.getClass().getSimpleName(), error.getMessage()))
                    .block();

            if (response == null) {
                throw new RuntimeException("Respuesta nula de Ollama");
            }

            String analysis = response.getResponse();

            if (analysis == null || analysis.trim().isEmpty()) {
                throw new RuntimeException("Respuesta vacía de Ollama");
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Análisis completado en {}ms - {} caracteres generados",
                    duration, analysis.length());

            return analysis;

        } catch (WebClientResponseException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("💥 Error HTTP de Ollama después de {}ms: {} - {}",
                    duration, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error de comunicación con Ollama: " + e.getMessage(), e);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("💥 Error general en generateAnalysis después de {}ms: {}",
                    duration, e.getMessage());
            throw new RuntimeException("Error generando análisis: " + e.getMessage(), e);
        }
    }

    public String generateAnalysis(String systemPrompt, String userData) {
        return generateAnalysis(systemPrompt, userData, defaultTimeout);
    }

    public CompletableFuture<String> generateAnalysisAsync(String systemPrompt, String userData) {
        return CompletableFuture.supplyAsync(() -> generateAnalysis(systemPrompt, userData, defaultTimeout));
    }

    public String quickAnalysis(String prompt) {
        return generateAnalysis(
                "Responde en máximo 50 palabras. Sé directo y conciso.",
                prompt,
                15);
    }

    public String generarAnalisisApicultura(String datosApiario, String tipoAnalisis) {
        String systemPrompt = """
                Eres un experto en apicultura. Analiza los siguientes datos y proporciona:
                - Resumen de la situación actual
                - 3 puntos clave de acción
                - Recomendaciones prácticas

                Responde en máximo 200 palabras con formato de bullet points.
                """;

        String contexto = String.format("""
                TIPO DE ANÁLISIS: %s

                DATOS DEL APIARIO:
                %s
                """, tipoAnalisis, datosApiario);

        // Cambio realizado: timeout aumentado a 60 segundos
        return generateAnalysis(systemPrompt, contexto, 60);
    }

    public String generateResponse(String prompt) {
        return generateAnalysis(
                "Responde de manera técnica pero clara en español. Sé conciso (máximo 100 palabras).",
                prompt,
                20);
    }

    public Map<String, Object> getOllamaStatus() {
        Map<String, Object> status = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            log.info("🔍 Ejecutando diagnóstico completo de Ollama...");

            String response = ollamaWebClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            boolean isRunning = response != null && !response.isEmpty();
            long responseTime = System.currentTimeMillis() - startTime;

            status.put("activo", isRunning);
            status.put("modeloConfigurado", model);
            status.put("tiempoRespuesta", responseTime + "ms");
            status.put("urlOllama", ollamaUrl);

            if (isRunning) {
                boolean modeloDisponible = response.contains(model);

                status.put("mensaje", "✅ Ollama activo y respondiendo");
                status.put("estado", "OPTIMO");
                status.put("modeloDisponible", modeloDisponible);

                if (!modeloDisponible) {
                    status.put("advertencia", "El modelo '" + model + "' no está descargado");
                    status.put("solucion", "Ejecutar: ollama pull " + model);
                }
            } else {
                status.put("mensaje", "❌ Ollama no responde");
                status.put("estado", "INACTIVO");
            }

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;

            status.put("activo", false);
            status.put("error", e.getMessage());
            status.put("tipoError", e.getClass().getSimpleName());
            status.put("mensaje", "❌ Error de conexión con Ollama");
            status.put("estado", "ERROR");
            status.put("tiempoRespuesta", responseTime + "ms");
            status.put("solucion", "Verificar que Ollama esté ejecutándose: ollama serve");
        }

        return status;
    }

    public Map<String, Object> diagnosticoExtendido() {
        Map<String, Object> diagnostico = new HashMap<>();

        try {
            log.info("🔍 Ejecutando diagnóstico extendido...");

            // Test 1: Verificar /api/tags
            long startTags = System.currentTimeMillis();
            String tagsResponse = ollamaWebClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            long tagsDuration = System.currentTimeMillis() - startTags;

            diagnostico.put("test1_tags", Map.of(
                    "exitoso", tagsResponse != null,
                    "tiempoMs", tagsDuration,
                    "tamañoRespuesta", tagsResponse != null ? tagsResponse.length() : 0,
                    "contieneModelos", tagsResponse != null && tagsResponse.contains("models")));

            // Test 2: Verificar generación simple
            try {
                log.info("🧪 Test de generación simple...");
                long startGen = System.currentTimeMillis();

                OllamaRequest testRequest = new OllamaRequest();
                testRequest.setModel(model);
                testRequest.setPrompt("Di 'OK' si estás funcionando");
                testRequest.setStream(false);

                Options opts = new Options();
                opts.setNumPredict(10);
                testRequest.setOptions(opts);

                OllamaResponse testResponse = ollamaWebClient.post()
                        .uri("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(testRequest), OllamaRequest.class)
                        .retrieve()
                        .bodyToMono(OllamaResponse.class)
                        .timeout(Duration.ofSeconds(15))
                        .block();

                long genDuration = System.currentTimeMillis() - startGen;

                diagnostico.put("test2_generacion", Map.of(
                        "exitoso", testResponse != null && testResponse.getResponse() != null,
                        "tiempoMs", genDuration,
                        "respuesta", testResponse != null ? testResponse.getResponse() : "null",
                        "modeloUsado", model));

            } catch (Exception genError) {
                diagnostico.put("test2_generacion", Map.of(
                        "exitoso", false,
                        "error", genError.getMessage(),
                        "tipoError", genError.getClass().getSimpleName()));
            }

            diagnostico.put("estado", "DIAGNOSTICO_COMPLETADO");

        } catch (Exception e) {
            diagnostico.put("error", e.getMessage());
            diagnostico.put("estado", "ERROR_DIAGNOSTICO");
        }

        return diagnostico;
    }

    public String getModel() {
        return model;
    }

    private String optimizarDatosEntrada(String userData) {
        if (userData == null || userData.isEmpty()) {
            return "";
        }

        int maxLength = 2000;
        if (userData.length() > maxLength) {
            log.info("📉 Recortando datos de {} a {} caracteres",
                    userData.length(), maxLength);
            return userData.substring(0, maxLength) + "\n\n[... datos truncados]";
        }

        return userData;
    }

    private String optimizarSystemPrompt(String systemPrompt) {
        if (systemPrompt == null || systemPrompt.isEmpty()) {
            return "";
        }

        int maxPromptLength = 500;
        if (systemPrompt.length() > maxPromptLength) {
            log.warn("⚠️ System prompt muy largo ({}), truncando a {}",
                    systemPrompt.length(), maxPromptLength);
            return systemPrompt.substring(0, maxPromptLength);
        }

        return systemPrompt;
    }
}
