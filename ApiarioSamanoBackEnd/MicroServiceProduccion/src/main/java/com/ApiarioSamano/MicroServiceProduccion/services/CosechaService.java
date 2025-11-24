package com.ApiarioSamano.MicroServiceProduccion.services;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.CosechaDTO.CosechaRequest;
import com.ApiarioSamano.MicroServiceProduccion.model.Cosecha;
import com.ApiarioSamano.MicroServiceProduccion.model.Lote;
import com.ApiarioSamano.MicroServiceProduccion.repository.CosechaRepository;
import com.ApiarioSamano.MicroServiceProduccion.repository.LoteRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CosechaService {

    private final CosechaRepository cosechaRepository;
    private final LoteRepository loteRepository;
    private static final Logger log = LoggerFactory.getLogger(CosechaService.class);

    @Transactional
    public CodigoResponse<Cosecha> guardarCosecha(CosechaRequest request) {
        try {
            log.info("Creando cosecha para lote ID: {}", request.getIdLote());

            // 🔹 Validar que el lote exista
            Optional<Lote> loteOpt = loteRepository.findById(request.getIdLote().longValue());
            if (loteOpt.isEmpty()) {
                log.warn("Lote con ID {} no encontrado", request.getIdLote());
                return new CodigoResponse<>(404, "Lote no encontrado", null);
            }

            Lote lote = loteOpt.get();

            // 🔹 VALIDACIÓN CRÍTICA: El tipo de cosecha debe ser IDÉNTICO al tipo de
            // producto del lote
            if (!validarTipoCosechaConLote(request.getTipoCosecha(), lote.getTipoProducto())) {
                log.warn("Tipo de cosecha no coincide con el tipo del lote. Cosecha: {}, Lote: {}",
                        request.getTipoCosecha(), lote.getTipoProducto());
                return new CodigoResponse<>(400,
                        String.format(
                                "El tipo de cosecha '%s' no coincide con el tipo de producto del lote '%s'. Deben ser idénticos.",
                                request.getTipoCosecha(), lote.getTipoProducto()),
                        null);
            }

            // 🔹 Crear cosecha
            Cosecha cosecha = new Cosecha();
            cosecha.setLote(lote);
            cosecha.setCalidad(request.getCalidad());
            cosecha.setCantidad(request.getCantidad());
            cosecha.setTipoCosecha(request.getTipoCosecha());
            cosecha.setIdApiario(request.getIdApiario().longValue());
            cosecha.setFechaCosecha(LocalDate.now());

            Cosecha guardada = cosechaRepository.save(cosecha);
            log.info("Cosecha guardada correctamente con ID: {} para lote: {}",
                    guardada.getId(), lote.getNumeroSeguimiento());

            return new CodigoResponse<>(200, "Cosecha guardada correctamente", guardada);
        } catch (Exception e) {
            log.error("Error al guardar la cosecha: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al guardar la cosecha: " + e.getMessage(), null);
        }
    }

    @Transactional
    public CodigoResponse<Cosecha> actualizarCosecha(Long id, CosechaRequest request) {
        try {
            log.info("Actualizando cosecha ID: {}", id);

            Optional<Cosecha> cosechaOptional = cosechaRepository.findById(id);
            if (cosechaOptional.isEmpty()) {
                return new CodigoResponse<>(404, "Cosecha no encontrada", null);
            }

            Cosecha cosecha = cosechaOptional.get();

            // 🔹 Si se cambia el lote, validar que el tipo de cosecha coincida con el nuevo
            // lote
            if (request.getIdLote() != null &&
                    !request.getIdLote().equals(cosecha.getLote().getId())) {

                Optional<Lote> nuevoLoteOpt = loteRepository.findById(request.getIdLote().longValue());
                if (nuevoLoteOpt.isEmpty()) {
                    return new CodigoResponse<>(404, "El nuevo lote especificado no existe", null);
                }

                Lote nuevoLote = nuevoLoteOpt.get();

                // 🔹 VALIDACIÓN: El tipo de cosecha debe coincidir con el nuevo lote
                String tipoCosecha = request.getTipoCosecha() != null ? request.getTipoCosecha()
                        : cosecha.getTipoCosecha();

                if (!validarTipoCosechaConLote(tipoCosecha, nuevoLote.getTipoProducto())) {
                    return new CodigoResponse<>(400,
                            String.format("El tipo de cosecha '%s' no coincide con el tipo del nuevo lote '%s'.",
                                    tipoCosecha, nuevoLote.getTipoProducto()),
                            null);
                }

                cosecha.setLote(nuevoLote);
            }

            // 🔹 Si se actualiza el tipo de cosecha, validar que coincida con el lote
            // actual
            if (request.getTipoCosecha() != null &&
                    !request.getTipoCosecha().equals(cosecha.getTipoCosecha())) {

                if (!validarTipoCosechaConLote(request.getTipoCosecha(), cosecha.getLote().getTipoProducto())) {
                    return new CodigoResponse<>(400,
                            String.format("El nuevo tipo de cosecha '%s' no coincide con el tipo del lote '%s'.",
                                    request.getTipoCosecha(), cosecha.getLote().getTipoProducto()),
                            null);
                }
            }

            // 🔹 Actualizar campos
            if (request.getCalidad() != null)
                cosecha.setCalidad(request.getCalidad());
            if (request.getCantidad() != null)
                cosecha.setCantidad(request.getCantidad());
            if (request.getTipoCosecha() != null)
                cosecha.setTipoCosecha(request.getTipoCosecha());
            if (request.getIdApiario() != null)
                cosecha.setIdApiario(request.getIdApiario().longValue());

            Cosecha actualizada = cosechaRepository.save(cosecha);
            log.info("Cosecha actualizada correctamente con ID: {}", id);

            return new CodigoResponse<>(200, "Cosecha actualizada correctamente", actualizada);
        } catch (Exception e) {
            log.error("Error al actualizar cosecha: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al actualizar cosecha: " + e.getMessage(), null);
        }
    }

    /**
     * 🔹 VALIDACIÓN: Compara si el tipo de cosecha es IDÉNTICO al tipo de producto
     * del lote
     * Permite diferentes niveles de flexibilidad según necesites
     */
    private boolean validarTipoCosechaConLote(String tipoCosecha, String tipoLote) {
        if (tipoCosecha == null || tipoLote == null) {
            return false;
        }

        // 🔹 OPCIÓN 1: Comparación EXACTA (case-sensitive)
        // return tipoCosecha.equals(tipoLote);

        // 🔹 OPCIÓN 2: Comparación EXACTA ignorando mayúsculas (recomendada)
        // return tipoCosecha.equalsIgnoreCase(tipoLote);

        // 🔹 OPCIÓN 3: Comparación FLEXIBLE (elimina espacios y normaliza)
        String cosechaNormalizado = normalizarTexto(tipoCosecha);
        String loteNormalizado = normalizarTexto(tipoLote);
        return cosechaNormalizado.equals(loteNormalizado);
    }

    /**
     * 🔹 Normaliza el texto para comparación flexible
     * - Convierte a minúsculas
     * - Elimina espacios extras
     * - Normaliza caracteres especiales
     */
    private String normalizarTexto(String texto) {
        if (texto == null)
            return "";

        return texto.toLowerCase()
                .trim()
                .replaceAll("\\s+", " ") // Elimina múltiples espacios
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n");
    }

    /**
     * 🔹 Obtener cosechas por lote con validación adicional
     */
    public CodigoResponse<List<Cosecha>> obtenerCosechasPorLote(Long idLote) {
        try {
            // Validar que el lote exista
            if (!loteRepository.existsById(idLote)) {
                return new CodigoResponse<>(404, "Lote no encontrado", null);
            }

            List<Cosecha> cosechas = cosechaRepository.findByLoteId(idLote);
            return new CodigoResponse<>(200,
                    String.format("Cosechas del lote %s obtenidas correctamente", idLote),
                    cosechas);
        } catch (Exception e) {
            log.error("Error al obtener cosechas por lote: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al obtener cosechas del lote", null);
        }
    }

    /**
     * 🔹 Obtener cosechas por apiario
     */
    public CodigoResponse<List<Cosecha>> obtenerCosechasPorApiario(Long idApiario) {
        try {
            List<Cosecha> cosechas = cosechaRepository.findByIdApiario(idApiario);
            return new CodigoResponse<>(200,
                    String.format("Cosechas del apiario %s obtenidas correctamente", idApiario),
                    cosechas);
        } catch (Exception e) {
            log.error("Error al obtener cosechas por apiario: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al obtener cosechas del apiario", null);
        }
    }

    /**
     * 🔹 Obtener cosechas por rango de fechas
     */
    public CodigoResponse<List<Cosecha>> obtenerCosechasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            if (fechaInicio == null || fechaFin == null) {
                return new CodigoResponse<>(400, "Las fechas de inicio y fin son requeridas", null);
            }

            if (fechaInicio.isAfter(fechaFin)) {
                return new CodigoResponse<>(400, "La fecha de inicio no puede ser posterior a la fecha fin", null);
            }

            List<Cosecha> cosechas = cosechaRepository.findByFechaCosechaBetween(fechaInicio, fechaFin);
            return new CodigoResponse<>(200,
                    String.format("Cosechas del rango %s a %s obtenidas correctamente", fechaInicio, fechaFin),
                    cosechas);
        } catch (Exception e) {
            log.error("Error al obtener cosechas por rango de fechas: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al obtener cosechas por rango de fechas", null);
        }
    }

    // ✅ Obtener todas las cosechas
    public CodigoResponse<List<Cosecha>> listarCosechas() {

        List<Cosecha> lista = cosechaRepository.findAll();
        return new CodigoResponse<>(200, "Listado de cosechas obtenido correctamente", lista);

    }

    // ✅ Buscar por ID
    public CodigoResponse<Cosecha> obtenerPorId(Long id) {
        try {
            return cosechaRepository.findById(id)
                    .map(c -> new CodigoResponse<>(200, "Cosecha encontrada", c))
                    .orElse(new CodigoResponse<>(404, "Cosecha no encontrada", null));
        } catch (Exception e) {
            log.error("Error al obtener cosecha por ID: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al obtener la cosecha", null);
        }
    }

    // ✅ Eliminar cosecha
    @Transactional
    public CodigoResponse<Void> eliminarCosecha(Long id) {
        try {
            if (!cosechaRepository.existsById(id)) {
                return new CodigoResponse<>(404, "Cosecha no encontrada", null);
            }
            cosechaRepository.deleteById(id);
            log.info("Cosecha eliminada correctamente con ID: {}", id);
            return new CodigoResponse<>(200, "Cosecha eliminada correctamente", null);
        } catch (Exception e) {
            log.error("Error al eliminar cosecha: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al eliminar la cosecha", null);
        }
    }

}