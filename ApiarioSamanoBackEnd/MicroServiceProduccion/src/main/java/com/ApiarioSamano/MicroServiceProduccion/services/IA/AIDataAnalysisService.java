package com.ApiarioSamano.MicroServiceProduccion.services.IA;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.ProductoDTO.ProductoResponse;
import com.ApiarioSamano.MicroServiceProduccion.model.Cosecha;
import com.ApiarioSamano.MicroServiceProduccion.model.Lote;
import com.ApiarioSamano.MicroServiceProduccion.model.Producto;
import com.ApiarioSamano.MicroServiceProduccion.services.CosechaService;
import com.ApiarioSamano.MicroServiceProduccion.services.LoteService;
import com.ApiarioSamano.MicroServiceProduccion.services.ProductoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class AIDataAnalysisService {

    private final CosechaService cosechaService;
    private final LoteService loteService;
    private final ProductoService productoService;
    private final OllamaService ollamaService;

    // 📊 ANÁLISIS ESTADÍSTICO COMPLETO PARA GRÁFICOS
    public CodigoResponse<Map<String, Object>> obtenerAnalisisEstadisticoProduccion() {
        long startTime = System.currentTimeMillis();

        try {
            log.info("📊 Iniciando análisis estadístico de producción...");

            // Obtener todos los datos usando métodos existentes
            List<Cosecha> cosechas = obtenerTodasCosechas();
            List<Lote> lotes = obtenerTodosLotes();
            List<ProductoResponse> productosResponse = obtenerTodosProductos();

            if (cosechas.isEmpty() && lotes.isEmpty() && productosResponse.isEmpty()) {
                return new CodigoResponse<>(204, "No hay datos de producción registrados", null);
            }

            // Convertir ProductoResponse a Producto para estadísticas
            List<Producto> productos = convertirProductosResponse(productosResponse);

            // Generar estadísticas para gráficos
            Map<String, Object> estadisticas = generarEstadisticasCompletas(cosechas, lotes, productos);

            // Preparar datos para IA
            String datosParaAnalisis = prepararDatosParaAnalisisProduccion(cosechas, lotes, productos);

            String systemPrompt = """
                    Eres un experto en análisis de producción apícola y optimización de procesos.
                    Analiza los datos de producción y proporciona:

                    1. RENDIMIENTO GENERAL: Evaluación del desempeño productivo
                    2. TENDENCIAS IDENTIFICADAS: Patrones en cosechas y lotes
                    3. OPTIMIZACIONES: Mejoras en eficiencia productiva
                    4. PRONÓSTICOS: Expectativas basadas en datos históricos

                    Sé específico, basado en datos y enfocado en métricas medibles.
                    Responde en formato estructurado con puntos clave.
                    responde en 300 palabras
                    """;

            String analisisIA = ollamaService.generateAnalysis(systemPrompt, datosParaAnalisis, 300);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("analisisIA", analisisIA);
            resultado.put("estadisticas", estadisticas);
            resultado.put("resumen", generarResumenEjecutivo(cosechas, lotes, productos));
            resultado.put("modeloUsado", ollamaService.getModel());
            resultado.put("tiempoProcesamiento", (System.currentTimeMillis() - startTime) + "ms");

            return new CodigoResponse<>(200, "Análisis estadístico de producción generado exitosamente", resultado);

        } catch (Exception e) {
            log.error("💥 Error en análisis estadístico de producción: {}", e.getMessage());
            return new CodigoResponse<>(500, "Error generando análisis: " + e.getMessage(), null);
        }
    }

    // 🔮 PREDICCIONES DE PRODUCCIÓN
    public CodigoResponse<Map<String, Object>> obtenerPrediccionesProduccion() {
        long startTime = System.currentTimeMillis();

        try {
            List<Cosecha> cosechas = obtenerTodasCosechas();

            if (cosechas.isEmpty()) {
                return new CodigoResponse<>(204, "No hay cosechas para análisis predictivo", null);
            }

            String datosHistoricos = prepararDatosHistoricosParaPrediccion(cosechas);

            String systemPrompt = """
                    Eres un especialista en predicción de producción apícola.
                    Basado en el historial de cosechas, predice:

                    1. PRODUCCIÓN ESTIMADA próximos 3 meses
                    2. RIESGOS IDENTIFICADOS: factores que pueden afectar producción
                    3. RECOMENDACIONES ESTRATÉGICAS: para maximizar rendimiento
                    4. INDICADORES CLAVE: métricas a monitorear

                    Usa análisis basado en tendencias históricas y factores estacionales.
                    Proporciona estimaciones cuantitativas cuando sea posible.
                    responde en 300 palabras
                    """;

            String prediccionesIA = ollamaService.generateAnalysis(systemPrompt, datosHistoricos, 300);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("prediccionesIA", prediccionesIA);
            resultado.put("datosAnalizados", generarResumenPredictivo(cosechas));
            resultado.put("modeloUsado", ollamaService.getModel());
            resultado.put("tiempoProcesamiento", (System.currentTimeMillis() - startTime) + "ms");

            return new CodigoResponse<>(200, "Predicciones de producción generadas exitosamente", resultado);

        } catch (Exception e) {
            log.error("💥 Error generando predicciones: {}", e.getMessage());
            return new CodigoResponse<>(500, "Error generando predicciones: " + e.getMessage(), null);
        }
    }

    // 💡 SUGERENCIAS POR COSECHA ESPECÍFICA
    public CodigoResponse<Map<String, Object>> obtenerSugerenciasCosecha(Long idCosecha) {
        long startTime = System.currentTimeMillis();

        try {
            CodigoResponse<Cosecha> cosechaResponse = cosechaService.obtenerPorId(idCosecha);
            if (cosechaResponse.getData() == null) {
                return new CodigoResponse<>(404, "Cosecha no encontrada", null);
            }
            Cosecha cosecha = cosechaResponse.getData();

            // Obtener lotes relacionados con esta cosecha
            CodigoResponse<List<Cosecha>> cosechasLoteResponse = cosechaService
                    .obtenerCosechasPorLote(cosecha.getLote().getId());
            List<Cosecha> cosechasRelacionadas = cosechasLoteResponse.getData() != null ? cosechasLoteResponse.getData()
                    : Collections.emptyList();

            // Obtener productos del lote
            CodigoResponse<List<ProductoResponse>> productosResponse = productoService
                    .obtenerProductosPorLote(cosecha.getLote().getId());
            List<ProductoResponse> productosRelacionados = productosResponse.getData() != null
                    ? productosResponse.getData()
                    : Collections.emptyList();

            String infoCompleta = prepararInfoCosechaCompleta(cosecha, cosechasRelacionadas, productosRelacionados);

            String systemPrompt = """
                    Eres un consultor especializado en optimización de cosechas apícolas.
                    Analiza esta cosecha específica y proporciona:

                    1. EVALUACIÓN ACTUAL: Estado y eficiencia
                    2. OPTIMIZACIONES INMEDIATAS: Mejoras aplicables ahora
                    3. PLAN ESTRATÉGICO: Acciones a mediano plazo
                    4. INDICADORES DE ÉXITO: Métricas para evaluar mejoras

                    Sé práctico, específico y basado en mejores prácticas apícolas.
                    responde en 300 palabras
                    """;

            String sugerenciasIA = ollamaService.generateAnalysis(systemPrompt, infoCompleta, 300);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("sugerenciasIA", sugerenciasIA);
            resultado.put("cosecha", Map.of(
                    "id", cosecha.getId(),
                    "fechaCosecha", cosecha.getFechaCosecha(),
                    "calidad", cosecha.getCalidad(),
                    "cantidad", cosecha.getCantidad(),
                    "tipoCosecha", cosecha.getTipoCosecha(),
                    "idApiario", cosecha.getIdApiario()));
            resultado.put("estadisticasRelacionadas",
                    generarEstadisticasCosecha(cosecha, cosechasRelacionadas, productosRelacionados));
            resultado.put("modeloUsado", ollamaService.getModel());
            resultado.put("tiempoProcesamiento", (System.currentTimeMillis() - startTime) + "ms");

            return new CodigoResponse<>(200, "Sugerencias para cosecha generadas exitosamente", resultado);

        } catch (Exception e) {
            log.error("💥 Error generando sugerencias: {}", e.getMessage());
            return new CodigoResponse<>(500, "Error generando sugerencias: " + e.getMessage(), null);
        }
    }

    // 📈 ANÁLISIS DE RENDIMIENTO POR PERIODO
    public CodigoResponse<Map<String, Object>> obtenerAnalisisRendimiento(String periodo) {
        long startTime = System.currentTimeMillis();

        try {
            List<Cosecha> cosechas = obtenerTodasCosechas();

            if (cosechas.isEmpty()) {
                return new CodigoResponse<>(204, "No hay datos para análisis de rendimiento", null);
            }

            List<Cosecha> cosechasFiltradas = filtrarCosechasPorPeriodo(cosechas, periodo);
            Map<String, Object> metricasRendimiento = calcularMetricasRendimiento(cosechasFiltradas);

            String datosRendimiento = prepararDatosRendimiento(cosechasFiltradas, metricasRendimiento);

            String systemPrompt = """
                    Eres un analista especializado en rendimiento productivo apícola.
                    Analiza las métricas de rendimiento y proporciona:

                    1. EVALUACIÓN DE EFICIENCIA: Cómo se está desempeñando la producción
                    2. BENCHMARKING: Comparación con estándares del sector
                    3. FACTORES CRÍTICOS: Elementos que más impactan el rendimiento
                    4. PLAN DE MEJORA: Acciones específicas para incrementar productividad

                    Enfócate en insights accionables y métricas comparativas.
                    responde en 300 palabras
                    """;

            String analisisRendimiento = ollamaService.generateAnalysis(systemPrompt, datosRendimiento, 300);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("analisisRendimiento", analisisRendimiento);
            resultado.put("metricas", metricasRendimiento);
            resultado.put("periodo", periodo);
            resultado.put("totalCosechasAnalizadas", cosechasFiltradas.size());
            resultado.put("modeloUsado", ollamaService.getModel());
            resultado.put("tiempoProcesamiento", (System.currentTimeMillis() - startTime) + "ms");

            return new CodigoResponse<>(200, "Análisis de rendimiento generado exitosamente", resultado);

        } catch (Exception e) {
            log.error("💥 Error en análisis de rendimiento: {}", e.getMessage());
            return new CodigoResponse<>(500, "Error generando análisis: " + e.getMessage(), null);
        }
    }

    // 🔍 MÉTODOS AUXILIARES PRIVADOS

    private List<Cosecha> obtenerTodasCosechas() {
        try {
            CodigoResponse<List<Cosecha>> response = cosechaService.listarCosechas();
            return response.getData() != null ? response.getData() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Error obteniendo cosechas: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Lote> obtenerTodosLotes() {
        try {
            CodigoResponse<List<Lote>> response = loteService.listarLotes();
            return response.getData() != null ? response.getData() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Error obteniendo lotes: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<ProductoResponse> obtenerTodosProductos() {
        try {
            CodigoResponse<List<ProductoResponse>> response = productoService.listarProductosActivos();
            return response.getData() != null ? response.getData() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Error obteniendo productos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Producto> convertirProductosResponse(List<ProductoResponse> productosResponse) {
        return productosResponse.stream()
                .map(resp -> {
                    Producto producto = new Producto();
                    producto.setId(resp.getId());
                    producto.setNombre(resp.getNombre());
                    producto.setPrecioMayoreo(resp.getPrecioMayoreo());
                    producto.setPrecioMenudeo(resp.getPrecioMenudeo());
                    producto.setTipoDeProducto(resp.getTipoDeProducto());
                    producto.setActivo(resp.getActivo());
                    return producto;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> generarEstadisticasCompletas(List<Cosecha> cosechas, List<Lote> lotes,
            List<Producto> productos) {
        Map<String, Object> stats = new HashMap<>();

        // Estadísticas de Cosechas
        if (!cosechas.isEmpty()) {
            stats.put("cosechas", Map.of(
                    "total", cosechas.size(),
                    "porCalidad", cosechas.stream().collect(Collectors.groupingBy(
                            c -> c.getCalidad() != null ? c.getCalidad() : "No especificada",
                            Collectors.counting())),
                    "porTipo", cosechas.stream().collect(Collectors.groupingBy(
                            c -> c.getTipoCosecha() != null ? c.getTipoCosecha() : "No especificado",
                            Collectors.counting())),
                    "cantidadTotal", cosechas.stream()
                            .map(Cosecha::getCantidad)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add),
                    "promedioPorCosecha", cosechas.stream()
                            .map(Cosecha::getCantidad)
                            .filter(Objects::nonNull)
                            .mapToDouble(BigDecimal::doubleValue)
                            .average()
                            .orElse(0.0)));
        }

        // Estadísticas de Lotes
        if (!lotes.isEmpty()) {
            stats.put("lotes", Map.of(
                    "total", lotes.size(),
                    "porTipoProducto", lotes.stream().collect(Collectors.groupingBy(
                            l -> l.getTipoProducto() != null ? l.getTipoProducto() : "No especificado",
                            Collectors.counting()))));
        }

        // Estadísticas de Productos
        if (!productos.isEmpty()) {
            stats.put("productos", Map.of(
                    "total", productos.size(),
                    "activos", productos.stream().filter(p -> p.getActivo() != null && p.getActivo()).count(),
                    "porTipo", productos.stream().collect(Collectors.groupingBy(
                            p -> p.getTipoDeProducto() != null ? p.getTipoDeProducto() : "No especificado",
                            Collectors.counting())),
                    "precioPromedioMayoreo", productos.stream()
                            .map(Producto::getPrecioMayoreo)
                            .filter(Objects::nonNull)
                            .mapToDouble(BigDecimal::doubleValue)
                            .average()
                            .orElse(0.0),
                    "precioPromedioMenudeo", productos.stream()
                            .map(Producto::getPrecioMenudeo)
                            .filter(Objects::nonNull)
                            .mapToDouble(BigDecimal::doubleValue)
                            .average()
                            .orElse(0.0)));
        }

        return stats;
    }

    private String prepararDatosParaAnalisisProduccion(List<Cosecha> cosechas, List<Lote> lotes,
            List<Producto> productos) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== RESUMEN GENERAL DE PRODUCCIÓN ===\n");
        sb.append("Total Cosechas: ").append(cosechas.size()).append("\n");
        sb.append("Total Lotes: ").append(lotes.size()).append("\n");
        sb.append("Total Productos: ").append(productos.size()).append("\n\n");

        sb.append("=== DETALLE DE COSECHAS ===\n");
        for (Cosecha c : cosechas) {
            sb.append("Cosecha ID: ").append(c.getId())
                    .append(" | Fecha: ").append(c.getFechaCosecha())
                    .append(" | Calidad: ").append(c.getCalidad())
                    .append(" | Tipo: ").append(c.getTipoCosecha())
                    .append(" | Cantidad: ").append(c.getCantidad())
                    .append(" | Apiario: ").append(c.getIdApiario())
                    .append("\n");
        }

        sb.append("\n=== DETALLE DE LOTES ===\n");
        for (Lote l : lotes) {
            sb.append("Lote ID: ").append(l.getId())
                    .append(" | Seguimiento: ").append(l.getNumeroSeguimiento())
                    .append(" | Tipo: ").append(l.getTipoProducto())
                    .append(" | Fecha: ").append(l.getFechaCreacion())
                    .append(" | Almacén: ").append(l.getIdAlmacen())
                    .append("\n");
        }

        sb.append("\n=== DETALLE DE PRODUCTOS ===\n");
        for (Producto p : productos) {
            sb.append("Producto ID: ").append(p.getId())
                    .append(" | Nombre: ").append(p.getNombre())
                    .append(" | Tipo: ").append(p.getTipoDeProducto())
                    .append(" | Mayoreo: $").append(p.getPrecioMayoreo())
                    .append(" | Menudeo: $").append(p.getPrecioMenudeo())
                    .append(" | Activo: ").append(p.getActivo())
                    .append("\n");
        }

        return sb.toString();
    }

    private String prepararDatosHistoricosParaPrediccion(List<Cosecha> cosechas) {
        StringBuilder sb = new StringBuilder();

        sb.append("HISTORIAL DE COSECHAS PARA PREDICCIÓN\n");
        sb.append("Total de registros históricos: ").append(cosechas.size()).append("\n\n");

        // Agrupar por mes para mostrar tendencias
        Map<String, List<Cosecha>> cosechasPorMes = cosechas.stream()
                .filter(c -> c.getFechaCosecha() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getFechaCosecha().getMonth().toString() + " " + c.getFechaCosecha().getYear()));

        for (Map.Entry<String, List<Cosecha>> entry : cosechasPorMes.entrySet()) {
            double cantidadTotal = entry.getValue().stream()
                    .map(Cosecha::getCantidad)
                    .filter(Objects::nonNull)
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();

            sb.append("Mes: ").append(entry.getKey())
                    .append(" | Cosechas: ").append(entry.getValue().size())
                    .append(" | Cantidad Total: ").append(cantidadTotal)
                    .append("\n");
        }

        // Estadísticas generales
        double cantidadPromedio = cosechas.stream()
                .map(Cosecha::getCantidad)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        sb.append("\nESTADÍSTICAS GENERALES:\n");
        sb.append("Cantidad promedio por cosecha: ").append(cantidadPromedio).append("\n");

        Optional<LocalDate> fechaMin = cosechas.stream()
                .map(Cosecha::getFechaCosecha)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo);
        Optional<LocalDate> fechaMax = cosechas.stream()
                .map(Cosecha::getFechaCosecha)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo);

        sb.append("Rango de fechas: ").append(fechaMin.orElse(LocalDate.now()))
                .append(" a ").append(fechaMax.orElse(LocalDate.now()))
                .append("\n");

        return sb.toString();
    }

    private String prepararInfoCosechaCompleta(Cosecha cosecha, List<Cosecha> cosechasRelacionadas,
            List<ProductoResponse> productosRelacionados) {
        StringBuilder sb = new StringBuilder();

        sb.append("INFORMACIÓN COMPLETA DE COSECHA\n");
        sb.append("ID: ").append(cosecha.getId()).append("\n");
        sb.append("Fecha: ").append(cosecha.getFechaCosecha()).append("\n");
        sb.append("Calidad: ").append(cosecha.getCalidad()).append("\n");
        sb.append("Tipo: ").append(cosecha.getTipoCosecha()).append("\n");
        sb.append("Cantidad: ").append(cosecha.getCantidad()).append("\n");
        sb.append("Apiario: ").append(cosecha.getIdApiario()).append("\n\n");

        sb.append("COSECHAS RELACIONADAS EN EL MISMO LOTE: ").append(cosechasRelacionadas.size()).append("\n");
        for (Cosecha c : cosechasRelacionadas) {
            sb.append("  - Cosecha ID: ").append(c.getId())
                    .append(" | Fecha: ").append(c.getFechaCosecha())
                    .append(" | Cantidad: ").append(c.getCantidad())
                    .append("\n");
        }

        sb.append("\nPRODUCTOS RELACIONADOS: ").append(productosRelacionados.size()).append("\n");
        for (ProductoResponse producto : productosRelacionados) {
            sb.append("  - ").append(producto.getNombre())
                    .append(" | Precio: $").append(producto.getPrecioMayoreo())
                    .append(" | Activo: ").append(producto.getActivo())
                    .append("\n");
        }

        return sb.toString();
    }

    private List<Cosecha> filtrarCosechasPorPeriodo(List<Cosecha> cosechas, String periodo) {
        LocalDate fechaLimite = LocalDate.now();

        switch (periodo.toLowerCase()) {
            case "semanal":
                fechaLimite = fechaLimite.minusWeeks(1);
                break;
            case "mensual":
                fechaLimite = fechaLimite.minusMonths(1);
                break;
            case "trimestral":
                fechaLimite = fechaLimite.minusMonths(3);
                break;
            case "anual":
                fechaLimite = fechaLimite.minusYears(1);
                break;
            default:
                // Últimos 30 días por defecto
                fechaLimite = fechaLimite.minusDays(30);
        }

        final LocalDate fechaFinal = fechaLimite;
        return cosechas.stream()
                .filter(c -> c.getFechaCosecha() != null && !c.getFechaCosecha().isBefore(fechaFinal))
                .collect(Collectors.toList());
    }

    private Map<String, Object> calcularMetricasRendimiento(List<Cosecha> cosechas) {
        Map<String, Object> metricas = new HashMap<>();

        if (cosechas.isEmpty()) {
            return metricas;
        }

        // Cálculos básicos
        double cantidadTotal = cosechas.stream()
                .map(Cosecha::getCantidad)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double cantidadPromedio = cosechas.stream()
                .map(Cosecha::getCantidad)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        // Distribución por calidad
        Map<String, Long> distribucionCalidad = cosechas.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCalidad() != null ? c.getCalidad() : "No especificada",
                        Collectors.counting()));

        metricas.put("totalCosechas", cosechas.size());
        metricas.put("cantidadTotal", cantidadTotal);
        metricas.put("cantidadPromedio", cantidadPromedio);
        metricas.put("distribucionCalidad", distribucionCalidad);

        Optional<LocalDate> fechaMin = cosechas.stream()
                .map(Cosecha::getFechaCosecha)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo);
        Optional<LocalDate> fechaMax = cosechas.stream()
                .map(Cosecha::getFechaCosecha)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo);

        metricas.put("rangoFechas", Map.of(
                "inicio", fechaMin.orElse(LocalDate.now()),
                "fin", fechaMax.orElse(LocalDate.now())));

        return metricas;
    }

    private String prepararDatosRendimiento(List<Cosecha> cosechas, Map<String, Object> metricas) {
        StringBuilder sb = new StringBuilder();

        sb.append("ANÁLISIS DE RENDIMIENTO - DATOS CRUDOS\n");
        sb.append("Período analizado: ").append(cosechas.size()).append(" cosechas\n");
        sb.append("Cantidad total producida: ").append(metricas.get("cantidadTotal")).append("\n");
        sb.append("Promedio por cosecha: ").append(metricas.get("cantidadPromedio")).append("\n\n");

        sb.append("DISTRIBUCIÓN POR CALIDAD:\n");
        @SuppressWarnings("unchecked")
        Map<String, Long> distribucion = (Map<String, Long>) metricas.get("distribucionCalidad");
        for (Map.Entry<String, Long> entry : distribucion.entrySet()) {
            double porcentaje = (entry.getValue() * 100.0) / cosechas.size();
            sb.append("  - ").append(entry.getKey()).append(": ")
                    .append(entry.getValue()).append(" (").append(String.format("%.1f", porcentaje)).append("%)\n");
        }

        sb.append("\nDETALLE DE COSECHAS:\n");
        for (Cosecha c : cosechas) {
            sb.append("  - Fecha: ").append(c.getFechaCosecha())
                    .append(" | Calidad: ").append(c.getCalidad())
                    .append(" | Cantidad: ").append(c.getCantidad())
                    .append(" | Tipo: ").append(c.getTipoCosecha())
                    .append("\n");
        }

        return sb.toString();
    }

    private Map<String, Object> generarResumenEjecutivo(List<Cosecha> cosechas, List<Lote> lotes,
            List<Producto> productos) {
        Map<String, Object> resumen = new HashMap<>();

        resumen.put("totalCosechas", cosechas.size());
        resumen.put("totalLotes", lotes.size());
        resumen.put("totalProductos", productos.size());
        resumen.put("productosActivos", productos.stream().filter(p -> p.getActivo() != null && p.getActivo()).count());

        if (!cosechas.isEmpty()) {
            Optional<LocalDate> ultimaCosecha = cosechas.stream()
                    .map(Cosecha::getFechaCosecha)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo);
            resumen.put("ultimaCosecha", ultimaCosecha.orElse(null));
        }

        return resumen;
    }

    private Map<String, Object> generarResumenPredictivo(List<Cosecha> cosechas) {
        Map<String, Object> resumen = new HashMap<>();

        resumen.put("totalRegistrosHistoricos", cosechas.size());

        Optional<LocalDate> fechaMin = cosechas.stream()
                .map(Cosecha::getFechaCosecha)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo);
        Optional<LocalDate> fechaMax = cosechas.stream()
                .map(Cosecha::getFechaCosecha)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo);

        resumen.put("rangoTemporal", Map.of(
                "inicio", fechaMin.orElse(LocalDate.now()),
                "fin", fechaMax.orElse(LocalDate.now())));

        // Calcular tendencia simple
        if (cosechas.size() >= 2) {
            List<Cosecha> cosechasOrdenadas = cosechas.stream()
                    .filter(c -> c.getFechaCosecha() != null && c.getCantidad() != null)
                    .sorted(Comparator.comparing(Cosecha::getFechaCosecha))
                    .collect(Collectors.toList());

            if (cosechasOrdenadas.size() >= 2) {
                Cosecha primera = cosechasOrdenadas.get(0);
                Cosecha ultima = cosechasOrdenadas.get(cosechasOrdenadas.size() - 1);

                double diferencia = ultima.getCantidad().doubleValue() - primera.getCantidad().doubleValue();
                double porcentajeCambio = (diferencia / primera.getCantidad().doubleValue()) * 100;
                resumen.put("tendenciaGeneral", porcentajeCambio >= 0 ? "POSITIVA" : "NEGATIVA");
                resumen.put("cambioPorcentual", String.format("%.1f%%", porcentajeCambio));
            }
        }

        return resumen;
    }

    private Map<String, Object> generarEstadisticasCosecha(Cosecha cosecha, List<Cosecha> cosechasRelacionadas,
            List<ProductoResponse> productosRelacionados) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("cosecha", Map.of(
                "id", cosecha.getId(),
                "fecha", cosecha.getFechaCosecha(),
                "calidad", cosecha.getCalidad(),
                "tipo", cosecha.getTipoCosecha(),
                "cantidad", cosecha.getCantidad(),
                "idApiario", cosecha.getIdApiario()));

        stats.put("cosechasRelacionadas", cosechasRelacionadas.size());
        stats.put("productosRelacionados", productosRelacionados.size());
        stats.put("valorEstimado", calcularValorEstimado(productosRelacionados));

        return stats;
    }

    private BigDecimal calcularValorEstimado(List<ProductoResponse> productos) {
        return productos.stream()
                .map(ProductoResponse::getPrecioMayoreo)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}