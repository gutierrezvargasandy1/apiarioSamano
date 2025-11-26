package com.ApiarioSamano.MicroServiceApiarios.controller;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.service.IA.AIDataAnalysisService;
import com.ApiarioSamano.MicroServiceApiarios.service.IA.OllamaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ia-analisis")
@RequiredArgsConstructor
@Log4j2
public class AIAnalysisController {

    private final AIDataAnalysisService aiDataAnalysisService;
    private final OllamaService ollamaService;

    @GetMapping("/predicciones")
    public CodigoResponse<Map<String, Object>> obtenerPrediccionesSalud() {
        log.info("📍 SOLICITANDO PREDICCIONES CON GEMMA3:4B");
        return aiDataAnalysisService.obtenerPrediccionesDeCuidado();
    }

    @GetMapping("/diagnostico-completo")
    public CodigoResponse<Map<String, Object>> diagnosticoCompleto() {
        Map<String, Object> diagnostico = new HashMap<>();

        try {
            log.info("🔍 DIAGNÓSTICO COMPLETO GEMA3:4B");

            // 1. Estado de Ollama
            Map<String, Object> ollamaStatus = ollamaService.getOllamaStatus();
            diagnostico.put("ollamaStatus", ollamaStatus);

            boolean ollamaOk = Boolean.TRUE.equals(ollamaStatus.get("ollamaDisponible")) &&
                    Boolean.TRUE.equals(ollamaStatus.get("modeloDisponible"));

            if (!ollamaOk) {
                diagnostico.put("estado", "OLLAMA_INCORRECTO");
                return new CodigoResponse<>(503, "Problema con Ollama", diagnostico);
            }

            // 2. Test de generación
            log.info("🧪 Probando generación con Gemma3:4b...");
            try {
                String testResponse = ollamaService.generateAnalysis(
                        "Responde únicamente con 'GEMA3_OK'",
                        "Test de conexión",
                        30);

                diagnostico.put("testGeneracion", "EXITOSO");
                diagnostico.put("respuestaTest", testResponse);
                diagnostico.put("estado", "GEMA3_FUNCIONAL");

            } catch (Exception e) {
                diagnostico.put("testGeneracion", "FALLIDO");
                diagnostico.put("errorGeneracion", e.getMessage());
                diagnostico.put("estado", "ERROR_GENERACION");
            }

            return new CodigoResponse<>(200, "Diagnóstico completado", diagnostico);

        } catch (Exception e) {
            log.error("❌ Error en diagnóstico: {}", e.getMessage());
            diagnostico.put("error", e.getMessage());
            return new CodigoResponse<>(500, "Error en diagnóstico", diagnostico);
        }
    }

    @GetMapping("/salud")
    public CodigoResponse<Map<String, Object>> verificarSaludOllama() {
        try {
            Map<String, Object> status = ollamaService.getOllamaStatus();
            boolean isActive = Boolean.TRUE.equals(status.get("ollamaDisponible")) &&
                    Boolean.TRUE.equals(status.get("modeloDisponible"));

            return new CodigoResponse<>(
                    isActive ? 200 : 503,
                    isActive ? "Gemma3:4b disponible" : "Ollama no disponible",
                    status);
        } catch (Exception e) {
            log.error("Error verificando salud: {}", e.getMessage());
            return new CodigoResponse<>(500, "Error verificando estado", null);
        }
    }
}