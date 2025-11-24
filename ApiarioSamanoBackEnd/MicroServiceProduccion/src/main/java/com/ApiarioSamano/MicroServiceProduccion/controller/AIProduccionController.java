package com.ApiarioSamano.MicroServiceProduccion.controller;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.services.IA.AIDataAnalysisService;
import com.ApiarioSamano.MicroServiceProduccion.services.IA.OllamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/produccion/ia")
@RequiredArgsConstructor
public class AIProduccionController {

    private final AIDataAnalysisService aiDataAnalysisService;
    private final OllamaService ollamaService;

    // 📊 ANÁLISIS ESTADÍSTICO COMPLETO
    @GetMapping("/estadisticas")
    public CodigoResponse<Map<String, Object>> obtenerAnalisisEstadistico() {
        return aiDataAnalysisService.obtenerAnalisisEstadisticoProduccion();
    }

    // 🔮 PREDICCIONES DE PRODUCCIÓN
    @GetMapping("/predicciones")
    public CodigoResponse<Map<String, Object>> obtenerPrediccionesProduccion() {
        return aiDataAnalysisService.obtenerPrediccionesProduccion();
    }

    // 💡 SUGERENCIAS POR COSECHA
    @GetMapping("/sugerencias/cosecha/{idCosecha}")
    public CodigoResponse<Map<String, Object>> obtenerSugerenciasCosecha(@PathVariable Long idCosecha) {
        return aiDataAnalysisService.obtenerSugerenciasCosecha(idCosecha);
    }

    // 📈 ANÁLISIS DE RENDIMIENTO
    @GetMapping("/rendimiento/{periodo}")
    public CodigoResponse<Map<String, Object>> obtenerAnalisisRendimiento(@PathVariable String periodo) {
        return aiDataAnalysisService.obtenerAnalisisRendimiento(periodo);
    }

    // 🏥 SALUD DEL SISTEMA
    @GetMapping("/salud")
    public CodigoResponse<Map<String, Object>> verificarSaludOllama() {
        boolean ollamaRunning = ollamaService.isOllamaRunning();
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ollamaDisponible", ollamaRunning);
        respuesta.put("mensaje", ollamaRunning ? "✅ Ollama está funcionando correctamente"
                : "❌ Ollama no disponible. Ejecuta: ollama serve");
        respuesta.put("modeloConfigurado", ollamaService.getModel());

        return new CodigoResponse<>(
                ollamaRunning ? 200 : 503,
                ollamaRunning ? "Sistema de IA operativo" : "Sistema de IA no disponible",
                respuesta);
    }

    // 💬 CONSULTA PERSONALIZADA SOBRE PRODUCCIÓN
    @PostMapping("/consulta")
    public CodigoResponse<Map<String, Object>> consultaPersonalizada(@RequestBody Map<String, String> request) {
        String pregunta = request.get("pregunta");
        String respuesta = ollamaService.generateResponse(pregunta);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("consulta", pregunta);
        resultado.put("respuesta", respuesta);
        resultado.put("modeloUsado", ollamaService.getModel());

        return new CodigoResponse<>(200, "Consulta procesada exitosamente", resultado);
    }

    // 📋 DIAGNÓSTICO COMPLETO
    @GetMapping("/diagnostico")
    public CodigoResponse<Map<String, Object>> diagnosticoCompleto() {
        Map<String, Object> diagnostico = ollamaService.diagnosticoExtendido();
        return new CodigoResponse<>(200, "Diagnóstico del sistema de IA", diagnostico);
    }
}