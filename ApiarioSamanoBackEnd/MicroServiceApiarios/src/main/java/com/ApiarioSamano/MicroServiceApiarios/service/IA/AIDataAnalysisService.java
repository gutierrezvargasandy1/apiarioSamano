package com.ApiarioSamano.MicroServiceApiarios.service.IA;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.model.Apiarios;
import com.ApiarioSamano.MicroServiceApiarios.service.ApiariosService;
import com.ApiarioSamano.MicroServiceApiarios.service.TemperaturaApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class AIDataAnalysisService {

    private final ApiariosService apiariosService;
    private final OllamaService ollamaService;
    private final TemperaturaApi temperaturaApia;

    public CodigoResponse<Map<String, Object>> obtenerPrediccionesDeCuidado() {
        long startTime = System.currentTimeMillis();

        try {
            log.info("🔮 Iniciando predicciones con Gemma3:4b...");

            // 1. Verificar estado de Ollama
            Map<String, Object> ollamaStatus = ollamaService.getOllamaStatus();
            log.info("📊 Estado Ollama: {}", ollamaStatus);

            if (!Boolean.TRUE.equals(ollamaStatus.get("ollamaDisponible"))) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "Ollama no disponible");
                errorResult.put("detalles", ollamaStatus);
                errorResult.put("solucion", "Verificar: docker logs ollama");
                log.warn("⚠️ Ollama no disponible. Detalles: {}", ollamaStatus);
                return new CodigoResponse<>(503, "Servicio de IA no disponible", errorResult);
            }

            // 2. Obtener apiarios
            CodigoResponse<List<Apiarios>> apiariosResponse = apiariosService.obtenerTodos();
            List<Apiarios> apiarios = apiariosResponse.getData();

            if (apiarios == null || apiarios.isEmpty()) {
                log.warn("⚠️ No hay apiarios registrados para analizar");
                return new CodigoResponse<>(204, "No hay apiarios registrados", null);
            }

            log.info("📦 Apiarios obtenidos: {}", apiarios.size());

            // 3. Obtener temperatura actual
            double temperaturaActual = 25.0; // Valor por defecto
            try {
                temperaturaActual = temperaturaApia.obtenerTemperaturaActualDolores();
                log.info("🌡️ Temperatura actual: {}°C", temperaturaActual);
            } catch (Exception e) {
                log.warn("⚠️ No se pudo obtener temperatura: {}. Usando valor por defecto: {}°C",
                        e.getMessage(), temperaturaActual);
            }

            // 4. Preparar datos para análisis
            String datos = prepararDatosParaGemma(apiarios);
            log.info("📝 Datos preparados: {} caracteres", datos.length());

            // 5. Prompt optimizado para Gemma3
            String systemPrompt = """
                    Eres Gemma, un asistente especializado en apicultura.
                    Analiza los datos proporcionados y genera predicciones CONCRETAS para los próximos 30 días.

                    FORMATO DE RESPUESTA REQUERIDO:

                    🔴 RIESGOS PRINCIPALES:
                    1. [Riesgo] - Probabilidad: [Alta/Media/Baja]
                       Acción: [Medida preventiva específica]

                    2. [Riesgo] - Probabilidad: [Alta/Media/Baja]
                       Acción: [Medida preventiva específica]

                    3. [Riesgo] - Probabilidad: [Alta/Media/Baja]
                       Acción: [Medida preventiva específica]

                    INSTRUCCIONES:
                    - Sé técnico, conciso y específico
                    - Máximo 150 palabras en total
                    - No incluyas introducciones ni conclusiones
                    - Basa tus predicciones en los datos de salud, tratamientos y clima
                    """;

            String contextoClimatico = String.format(
                    "🌍 CONTEXTO:\n" +
                            "Ubicación: Dolores Hidalgo, Guanajuato, México\n" +
                            "Temperatura actual: %.1f°C\n" +
                            "Total apiarios: %d\n\n",
                    temperaturaActual, apiarios.size());

            String entradaIA = contextoClimatico + datos;

            log.info("📊 Prompt preparado:");
            log.info("   - System prompt: {} caracteres", systemPrompt.length());
            log.info("   - Entrada de datos: {} caracteres", entradaIA.length());
            log.info("   - Temperatura: {}°C", temperaturaActual);

            // 6. Ejecutar análisis con Ollama
            log.info("🧠 Solicitando análisis a Gemma3...");
            String prediccionIA = ollamaService.generateAnalysis(systemPrompt, entradaIA, 220);

            if (prediccionIA == null || prediccionIA.trim().isEmpty()) {
                throw new RuntimeException("El modelo no generó ninguna predicción");
            }

            log.info("✅ Predicción generada: {} caracteres", prediccionIA.length());

            // 7. Construir respuesta completa
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("prediccionesIA", prediccionIA);
            resultado.put("apiariosAnalizados", apiarios.size());
            resultado.put("modeloUsado", ollamaService.getModel());
            resultado.put("ubicacion", "Dolores Hidalgo, Guanajuato, México");
            resultado.put("temperaturaActual", String.format("%.1f°C", temperaturaActual));
            resultado.put("estadoOllama", Map.of(
                    "disponible", ollamaStatus.get("ollamaDisponible"),
                    "modelo", ollamaStatus.get("modeloConfigurado"),
                    "url", ollamaStatus.get("url")));
            resultado.put("tiempoProcesamiento", (System.currentTimeMillis() - startTime) + "ms");
            resultado.put("timestamp", java.time.LocalDateTime.now().toString());

            // Estadísticas adicionales
            Map<String, Object> estadisticas = obtenerEstadisticas(apiarios);
            resultado.put("estadisticas", estadisticas);

            log.info("✅ Predicciones generadas exitosamente en {}ms",
                    (System.currentTimeMillis() - startTime));

            return new CodigoResponse<>(200, "Predicciones generadas con Gemma3:4b", resultado);

        } catch (RuntimeException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("💥 Error en predicciones después de {}ms: {}", duration, e.getMessage(), e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("tipoError", e.getClass().getSimpleName());
            errorResult.put("modelo", ollamaService.getModel());
            errorResult.put("url", ollamaService.getOllamaUrl());
            errorResult.put("duracion", duration + "ms");
            errorResult.put("timestamp", java.time.LocalDateTime.now().toString());
            errorResult.put("solucion",
                    "Verificar: 1) Ollama en ejecución (docker ps), " +
                            "2) Modelo gemma3:4b disponible (docker exec -it ollama ollama list), " +
                            "3) Logs de Ollama (docker logs ollama)");

            return new CodigoResponse<>(500, "Error generando predicciones: " + e.getMessage(), errorResult);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("💥 Error crítico inesperado después de {}ms: {}", duration, e.getMessage(), e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Error interno del servidor");
            errorResult.put("mensaje", e.getMessage());
            errorResult.put("tipoError", e.getClass().getSimpleName());
            errorResult.put("duracion", duration + "ms");
            errorResult.put("timestamp", java.time.LocalDateTime.now().toString());

            return new CodigoResponse<>(500, "Error interno al generar predicciones", errorResult);
        }
    }

    /**
     * Prepara los datos de los apiarios en formato legible para Gemma3
     */
    private String prepararDatosParaGemma(List<Apiarios> apiarios) {
        if (apiarios == null || apiarios.isEmpty()) {
            return "No hay datos de apiarios disponibles.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📊 DATOS DE APIARIOS:\n\n");

        // 1. Resumen general
        sb.append("RESUMEN GENERAL:\n");
        sb.append("- Total de apiarios: ").append(apiarios.size()).append("\n");

        // 2. Estadísticas de tratamiento (✅ CORREGIDO: Receta es objeto)
        long conTratamiento = apiarios.stream()
                .filter(a -> a.getReceta() != null)
                .count();
        long sinTratamiento = apiarios.size() - conTratamiento;

        sb.append("- Con tratamiento activo: ").append(conTratamiento)
                .append(" (").append(String.format("%.1f%%", (conTratamiento * 100.0 / apiarios.size())))
                .append(")\n");
        sb.append("- Sin tratamiento: ").append(sinTratamiento)
                .append(" (").append(String.format("%.1f%%", (sinTratamiento * 100.0 / apiarios.size())))
                .append(")\n\n");

        // 3. Distribución de salud
        Map<String, Long> saludStats = apiarios.stream()
                .collect(Collectors.groupingBy(
                        a -> {
                            String salud = a.getSalud();
                            if (salud == null || salud.trim().isEmpty()) {
                                return "No especificada";
                            }
                            return salud;
                        },
                        Collectors.counting()));

        sb.append("ESTADO DE SALUD:\n");
        saludStats.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .forEach(entry -> {
                    double porcentaje = (entry.getValue() * 100.0) / apiarios.size();
                    sb.append("- ").append(entry.getKey()).append(": ")
                            .append(entry.getValue())
                            .append(" (").append(String.format("%.1f%%", porcentaje)).append(")\n");
                });

        sb.append("\n");

        // 4. Apiarios con problemas de salud críticos
        List<Apiarios> apiariosEnfermos = apiarios.stream()
                .filter(a -> {
                    String salud = a.getSalud();
                    if (salud == null)
                        return false;
                    String saludLower = salud.toLowerCase();
                    return saludLower.contains("enferm") ||
                            saludLower.contains("crítico") ||
                            saludLower.contains("grave");
                })
                .toList();

        if (!apiariosEnfermos.isEmpty()) {
            sb.append("⚠️ APIARIOS CON ATENCIÓN PRIORITARIA:\n");
            apiariosEnfermos.forEach(a -> {
                sb.append("- Apiario ID: ").append(a.getId())
                        .append(", Estado: ").append(a.getSalud());

                // ✅ CORREGIDO: Receta es objeto, mostrar si tiene receta y su descripción
                if (a.getReceta() != null) {
                    sb.append(", Con tratamiento activo");
                    String descripcion = a.getReceta().getDescripcion();
                    if (descripcion != null && !descripcion.trim().isEmpty()) {
                        // Limitar la descripción a 50 caracteres para no saturar
                        String descCorta = descripcion.length() > 50
                                ? descripcion.substring(0, 50) + "..."
                                : descripcion;
                        sb.append(" (").append(descCorta).append(")");
                    }
                }
                sb.append("\n");
            });
            sb.append("\n");
        }

        // 5. Contexto climático
        sb.append("CONTEXTO CLIMÁTICO:\n");
        sb.append("- Región: Dolores Hidalgo, Guanajuato, México\n");
        sb.append("- Clima: Templado semiárido\n");
        sb.append("- Periodo de análisis: Próximos 30 días\n");
        sb.append("- Consideraciones: Temporada de invierno, posibles heladas\n");

        return sb.toString();
    }

    /**
     * Obtiene estadísticas resumidas de los apiarios
     */
    private Map<String, Object> obtenerEstadisticas(List<Apiarios> apiarios) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalApiarios", apiarios.size());

        // ✅ CORREGIDO: Receta es objeto
        long conTratamiento = apiarios.stream()
                .filter(a -> a.getReceta() != null)
                .count();
        stats.put("conTratamiento", conTratamiento);
        stats.put("sinTratamiento", apiarios.size() - conTratamiento);

        // Distribución de salud
        Map<String, Long> saludDistribucion = apiarios.stream()
                .collect(Collectors.groupingBy(
                        a -> {
                            String salud = a.getSalud();
                            if (salud == null || salud.trim().isEmpty()) {
                                return "No especificada";
                            }
                            return salud;
                        },
                        Collectors.counting()));
        stats.put("distribucionSalud", saludDistribucion);

        // Contar apiarios en estado crítico
        long estadoCritico = apiarios.stream()
                .filter(a -> {
                    String salud = a.getSalud();
                    if (salud == null)
                        return false;
                    String saludLower = salud.toLowerCase();
                    return saludLower.contains("enferm") ||
                            saludLower.contains("crítico") ||
                            saludLower.contains("grave");
                })
                .count();
        stats.put("apiariosEnEstadoCritico", estadoCritico);

        // ✅ NUEVO: Detalles de tratamientos
        long conMedicamentos = apiarios.stream()
                .filter(a -> a.getReceta() != null &&
                        a.getReceta().getMedicamentos() != null &&
                        !a.getReceta().getMedicamentos().isEmpty())
                .count();
        stats.put("apiariosConMedicamentos", conMedicamentos);

        return stats;
    }
}