package com.ApiarioSamano.MicroServiceApiarios.controller;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.service.IA.AIDataAnalysisService;
import com.ApiarioSamano.MicroServiceApiarios.service.IA.OllamaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ia-analisis")
@RequiredArgsConstructor
@Log4j2
public class AIAnalysisController {

    private final AIDataAnalysisService aiDataAnalysisService;
    private final OllamaService ollamaService;

    @GetMapping("/estadisticas")
    public CodigoResponse<Map<String, Object>> obtenerAnalisisEstadistico() {
        return aiDataAnalysisService.obtenerDatosEstadisticosApiarios();
    }

    @GetMapping("/predicciones")
    public CodigoResponse<Map<String, Object>> obtenerPrediccionesSalud() {
        return aiDataAnalysisService.obtenerPrediccionesDeCuidado();
    }

    @GetMapping("/recomendaciones/{idApiario}")
    public CodigoResponse<Map<String, Object>> obtenerRecomendacionesPersonalizadas(
            @PathVariable Long idApiario) {
        return aiDataAnalysisService.obtenerSugerenciasPorApiario(idApiario);
    }

    // 💬 CONSULTA PERSONALIZADA SOBRE PRODUCCIÓN
    @PostMapping("/consulta")
    public CodigoResponse<Map<String, Object>> consultaPersonalizada(@RequestBody Map<String, String> request) {
        String pregunta = request.get("pregunta");
        return aiDataAnalysisService.consultaPersonalizada(pregunta);
    }

    @GetMapping("/salud")
    public CodigoResponse<Map<String, Object>> verificarSaludOllama() {
        boolean ollamaRunning = ollamaService.isOllamaRunning();
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ollamaDisponible", ollamaRunning);
        respuesta.put("mensaje", ollamaRunning ? "Ollama está funcionando correctamente"
                : "Ollama no está disponible. Ejecuta: ollama serve");

        return new CodigoResponse<>(
                ollamaRunning ? 200 : 503,
                ollamaRunning ? "Ollama disponible" : "Ollama no disponible",
                respuesta);
    }

    @GetMapping("/diagnostico")
    public CodigoResponse<Map<String, Object>> diagnosticoCompleto() {
        Map<String, Object> status = ollamaService.getOllamaStatus();
        boolean isActive = Boolean.TRUE.equals(status.get("activo"));

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("diagnostico", status);
        resultado.put("timestamp", LocalDateTime.now());

        if (isActive) {
            try {
                log.info("🧪 Probando generación de respuesta real...");
                String testResponse = ollamaService.generateResponse(
                        "Responde 'FUNCIONANDO' si estás listo para análisis apícola.");
                resultado.put("testConexion", testResponse);
                resultado.put("estado", "SISTEMA_IA_LISTO");
                resultado.put("testExitoso", true);
            } catch (Exception e) {
                log.error("❌ Error en test de generación: {}", e.getMessage());
                resultado.put("testConexion", "ERROR: " + e.getMessage());
                resultado.put("estado", "CONEXION_FALLIDA");
                resultado.put("testExitoso", false);
                resultado.put("errorDetalle", e.getClass().getSimpleName());
            }
        } else {
            resultado.put("estado", "OLLAMA_NO_DISPONIBLE");
            resultado.put("solucion", "Ejecutar 'ollama serve' en terminal");
        }

        return new CodigoResponse<>(
                isActive ? 200 : 503,
                isActive ? "Diagnóstico completado" : "Ollama no disponible",
                resultado);
    }

    @GetMapping("/diagnostico-extendido")
    public CodigoResponse<Map<String, Object>> diagnosticoExtendido() {
        try {
            log.info("🔍 Ejecutando diagnóstico extendido...");
            Map<String, Object> diagnostico = ollamaService.diagnosticoExtendido();

            return new CodigoResponse<>(200,
                    "Diagnóstico extendido completado",
                    diagnostico);

        } catch (Exception e) {
            log.error("❌ Error en diagnóstico extendido: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("tipo", e.getClass().getSimpleName());

            return new CodigoResponse<>(500,
                    "Error en diagnóstico: " + e.getMessage(),
                    error);
        }
    }

    @GetMapping("/test-simple")
    public CodigoResponse<Map<String, Object>> testGeneracionSimple() {
        Map<String, Object> resultado = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            log.info("🧪 Iniciando test de generación simple...");

            boolean disponible = ollamaService.isOllamaRunning();
            resultado.put("ollamaDisponible", disponible);

            if (!disponible) {
                resultado.put("mensaje", "Ollama no está disponible");
                return new CodigoResponse<>(503, "Ollama no disponible", resultado);
            }

            String respuesta = ollamaService.generateResponse("Di 'OK'");
            resultado.put("respuestaIA", respuesta);
            resultado.put("longitudRespuesta", respuesta.length());
            resultado.put("tiempoProcesamiento", (System.currentTimeMillis() - startTime) + "ms");
            resultado.put("estado", "EXITOSO");

            log.info("✅ Test simple completado exitosamente");
            return new CodigoResponse<>(200, "Test de generación exitoso", resultado);

        } catch (Exception e) {
            log.error("❌ Error en test simple: {}", e.getMessage(), e);
            resultado.put("error", e.getMessage());
            resultado.put("tipoError", e.getClass().getSimpleName());
            resultado.put("stackTrace", e.getStackTrace()[0].toString());
            resultado.put("tiempoProcesamiento", (System.currentTimeMillis() - startTime) + "ms");

            return new CodigoResponse<>(500, "Error en test: " + e.getMessage(), resultado);
        }
    }
}
