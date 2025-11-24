package com.ApiarioSamano.MicroServiceApiarios.service;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO.RecetaRequest;
import com.ApiarioSamano.MicroServiceApiarios.model.*;
import com.ApiarioSamano.MicroServiceApiarios.repository.*;
import com.ApiarioSamano.MicroServiceApiarios.service.MicroServicesAPI.MicroServiceClientMedicamentos;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class ApiariosService {

    private final ApiariosRepository apiariosRepository;
    private final RecetaRepository recetaRepository;
    private final HistorialMedicoRepository historialMedicoRepository;
    private final HistorialRecetasRepository historialRecetasRepository;
    private final RecetaMedicamentoRepository recetaMedicamentoRepository;
    @Autowired
    private MicroServiceClientMedicamentos microServiceClientMedicamentos;

    // 🟢 Crear nuevo apiario (usando DTO)
    public CodigoResponse crearApiario(ApiarioRequestDTO apiarioDTO) {
        Apiarios apiario = new Apiarios();

        apiario.setNumeroApiario(apiarioDTO.getNumeroApiario());
        apiario.setUbicacion(apiarioDTO.getUbicacion());
        apiario.setSalud(apiarioDTO.getSalud());

        // Crear historial médico inicial si no existe
        HistorialMedico historial = new HistorialMedico();
        historial.setNotas("Historial inicial del apiario.");
        log.info("✅ [CREAR APIARIO] Entidad lista para guardar: {}", historial);
        historialMedicoRepository.save(historial);
        apiario.setHistorialMedico(historial);
        apiario.setReceta(null);

        log.info("✅ [CREAR APIARIO] Entidad lista para guardar: {}", apiario);

        apiariosRepository.save(apiario);

        return new CodigoResponse<>(200, "Apiario creado exitosamente", apiario);
    }

    // 🟡 Modificar un apiario existente
    public CodigoResponse modificarApiario(Long id, ApiarioRequestDTO datosActualizados) {
        Apiarios apiario = apiariosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + id));

        apiario.setNumeroApiario(datosActualizados.getNumeroApiario());
        apiario.setUbicacion(datosActualizados.getUbicacion());
        apiario.setSalud(datosActualizados.getSalud());

        apiariosRepository.save(apiario);

        return new CodigoResponse<>(200, "Apiario actualizado correctamente", apiario);
    }

    // 🔴 Eliminar apiario sin afectar historial médico
    public CodigoResponse eliminarApiario(Long id) {
        Apiarios apiario = apiariosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + id));

        // Desvincular el historial antes de eliminar
        apiario.setHistorialMedico(null);
        apiariosRepository.delete(apiario);

        return new CodigoResponse<>(200, "Apiario eliminado correctamente (historial conservado)", null);
    }

    public CodigoResponse<Receta> agregarReceta(Long idApiario, RecetaRequest recetaDTO) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Iniciando proceso para agregar receta al apiario ID: {}", idApiario);

        // Buscar apiario
        Apiarios apiario = apiariosRepository.findById(idApiario)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + idApiario));
        logger.info("Apiario encontrado: {}", apiario.getNumeroApiario());

        // Crear entidad Receta a partir del DTO
        Receta receta = new Receta();
        receta.setDescripcion(recetaDTO.getDescripcion());

        // ✅ INICIALIZAR LA LISTA DE MEDICAMENTOS
        receta.setMedicamentos(new ArrayList<>());

        Receta recetaGuardada = recetaRepository.save(receta);
        logger.info("Receta guardada con ID: {}", recetaGuardada.getId());

        // Asociar medicamentos si vienen en el DTO
        if (recetaDTO.getMedicamentos() != null && !recetaDTO.getMedicamentos().isEmpty()) {
            logger.info("Asociando medicamentos a la receta...");

            List<RecetaMedicamento> listaMedicamentos = recetaDTO.getMedicamentos().stream().map(medDTO -> {
                // ✅ USAR EL NUEVO MÉTODO obtenerPorId (MÁS EFICIENTE)
                MedicamentosResponse med = microServiceClientMedicamentos.obtenerPorId(medDTO.getId());

                RecetaMedicamento rm = new RecetaMedicamento();
                rm.setReceta(recetaGuardada);
                rm.setIdMedicamento(med.getId());
                rm.setMedicamentoInfo(med); // ✅ CARGAR INFO COMPLETA
                logger.info("Medicamento asociado a receta: {}", med.getNombre());

                return rm;
            }).toList();

            // ✅ GUARDAR MEDICAMENTOS Y ASOCIARLOS A LA RECETA
            List<RecetaMedicamento> medicamentosGuardados = recetaMedicamentoRepository.saveAll(listaMedicamentos);

            // ✅ ASOCIAR LA LISTA A LA RECETA (IMPORTANTE)
            recetaGuardada.setMedicamentos(medicamentosGuardados);
            recetaRepository.save(recetaGuardada); // ✅ ACTUALIZAR RECETA CON MEDICAMENTOS

            logger.info("{} medicamentos guardados y asociados a la receta", medicamentosGuardados.size());
        }

        // Asociar receta al historial médico del apiario
        logger.info("INICIANDO HISTORIAL MEDICO");
        HistorialMedico historial = apiario.getHistorialMedico();
        if (historial == null) {
            logger.info("CREANDO HISTORIAL MEDICO PORQUE NO TENIA");
            historial = new HistorialMedico();
            historial.setNotas("Historial creado automáticamente al asignar receta.");
            historialMedicoRepository.save(historial);
            apiario.setHistorialMedico(historial);

            // ✅ GUARDAR APIARIO CON NUEVO HISTORIAL
            apiariosRepository.save(apiario);
            logger.info("Historial médico creado automáticamente para el apiario.");
        }

        // Guardar relación en historialrecetas
        HistorialRecetas hr = new HistorialRecetas();
        hr.setHistorialMedico(historial);
        hr.setReceta(recetaGuardada);
        historialRecetasRepository.save(hr);
        logger.info("Receta agregada al historial del apiario mediante historialrecetas.");

        // Asociar receta al apiario
        apiario.setReceta(recetaGuardada);
        apiariosRepository.save(apiario);
        logger.info("Receta asociada al apiario y cambios guardados.");

        // ✅ CARGAR RECETA COMPLETA CON MEDICAMENTOS (PARA EVITAR PROBLEMAS DE CACHE)
        Receta recetaCompleta = recetaRepository.findById(recetaGuardada.getId())
                .orElseThrow(() -> new RuntimeException("Error al cargar receta completa"));

        return new CodigoResponse<>(200, "Receta agregada correctamente", recetaCompleta);
    }

    // 🔍 Obtener historial médico por ID con recetas y medicamentos completos
    public CodigoResponse obtenerHistorialMedicoPorId(Long idHistorial) {
        HistorialMedico historial = historialMedicoRepository.findById(idHistorial)
                .orElseThrow(() -> new RuntimeException("Historial médico no encontrado con ID: " + idHistorial));

        // Obtener todas las recetas asociadas a este historial
        List<HistorialRecetas> historialRecetas = historialRecetasRepository.findByHistorialMedico(historial);

        // Cargar información completa de cada receta con sus medicamentos
        List<Receta> recetasCompletas = new ArrayList<>();

        for (HistorialRecetas hr : historialRecetas) {
            Receta receta = hr.getReceta();

            // Cargar información de medicamentos para esta receta
            if (receta.getMedicamentos() != null) {
                for (RecetaMedicamento rm : receta.getMedicamentos()) {
                    try {
                        MedicamentosResponse medicamentoInfo = microServiceClientMedicamentos
                                .obtenerPorId(rm.getIdMedicamento());
                        rm.setMedicamentoInfo(medicamentoInfo);
                    } catch (Exception e) {
                        log.warn("No se pudo cargar información del medicamento ID: {} para receta ID: {}",
                                rm.getIdMedicamento(), receta.getId());
                        rm.setMedicamentoInfo(null);
                    }
                }
            }
            recetasCompletas.add(receta);
        }

        // Crear un DTO o mapa con la información completa
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("historialMedico", historial);
        respuesta.put("recetas", recetasCompletas);
        respuesta.put("totalRecetas", recetasCompletas.size());

        return new CodigoResponse<>(200, "Historial médico obtenido exitosamente", respuesta);
    }

    // 🧹 Eliminar receta cumplida del apiario
    // 🧹 Marcar receta como cumplida y guardar en historial médico
    // 🧹 Marcar receta como cumplida y guardar en historial médico
    public CodigoResponse eliminarRecetaCumplida(Long idApiario) {
        try {
            log.info("🔄 Marcando receta como cumplida para apiario ID: {}", idApiario);

            Apiarios apiario = apiariosRepository.findById(idApiario)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + idApiario));

            Receta receta = apiario.getReceta();
            if (receta == null) {
                log.warn("⚠️ Apiario {} no tiene receta asignada", idApiario);
                return new CodigoResponse<>(400, "El apiario no tiene receta asignada.", null);
            }

            log.info("📋 Receta encontrada ID: {}, procediendo a marcar como cumplida", receta.getId());

            // ✅ OBTENER EL HISTORIAL MÉDICO DEL APIARIO
            HistorialMedico historial = apiario.getHistorialMedico();
            if (historial == null) {
                log.info("📝 Creando nuevo historial médico para el apiario");
                historial = new HistorialMedico();
                historial.setNotas(""); // Iniciar con notas vacías
                historial = historialMedicoRepository.save(historial);
                apiario.setHistorialMedico(historial);
                apiariosRepository.save(apiario);
            }

            // ✅ CREAR NUEVA NOTA PARA LA RECETA CUMPLIDA (REEMPLAZANDO EL HISTORIAL
            // INICIAL)
            String nuevaNota = String.format(
                    "✅ Receta cumplida - %s: Aplicada correctamente (Medicamentos: %d) - Fecha: %s",
                    receta.getDescripcion(),
                    receta.getMedicamentos() != null ? receta.getMedicamentos().size() : 0,
                    java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // ✅ REEMPLAZAR COMPLETAMENTE LAS NOTAS ANTERIORES
            // Si el historial tenía "Historial inicial del apiario", lo reemplazamos
            String notasActuales = historial.getNotas();

            if (notasActuales == null || notasActuales.trim().isEmpty() ||
                    notasActuales.contains("Historial inicial del apiario")) {
                // ✅ REEMPLAZAR COMPLETAMENTE con la nueva receta cumplida
                historial.setNotas(nuevaNota);
            } else {
                // ✅ Si ya tiene otras recetas cumplidas, agregar la nueva al final
                historial.setNotas(notasActuales + "\n" + nuevaNota);
            }

            historialMedicoRepository.save(historial);
            log.info("Historial médico actualizado con receta cumplida (reemplazado historial inicial)");

            // ✅ CREAR REGISTRO EN HISTORIALRECETAS PARA RELACIONAR LA RECETA CUMPLIDA
            HistorialRecetas historialReceta = new HistorialRecetas();
            historialReceta.setHistorialMedico(historial);
            historialReceta.setReceta(receta);
            historialRecetasRepository.save(historialReceta);
            log.info("📋 Receta agregada al historial de recetas cumplidas");

            // ✅ DESVINCULAR RECETA DEL APIARIO (PERO NO ELIMINARLA)
            apiario.setReceta(null);
            apiariosRepository.save(apiario);
            log.info("🔗 Receta desvinculada del apiario");

            // ✅ ACTUALIZAR DESCRIPCIÓN DE LA RECETA PARA INDICAR QUE FUE CUMPLIDA
            String descripcionOriginal = receta.getDescripcion();
            // Remover "CUMPLIDA - " si ya estaba presente para evitar duplicados
            if (descripcionOriginal.startsWith("CUMPLIDA - ")) {
                descripcionOriginal = descripcionOriginal.substring("CUMPLIDA - ".length());
            }
            receta.setDescripcion("CUMPLIDA - " + descripcionOriginal + " - Finalizada: " +
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            recetaRepository.save(receta);
            log.info(" Receta marcada como cumplida en su descripción");

            return new CodigoResponse<>(200,
                    "Receta marcada como cumplida y agregada al historial médico correctamente",
                    apiario);

        } catch (Exception e) {
            log.error("❌ Error al marcar receta como cumplida: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno al procesar receta cumplida: " + e.getMessage(), null);
        }
    }

    // 🔍 Obtener historial médico completo del apiario con recetas cumplidas
    public CodigoResponse obtenerHistorialCompletoApiario(Long idApiario) {
        try {
            Apiarios apiario = apiariosRepository.findById(idApiario)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + idApiario));

            HistorialMedico historial = apiario.getHistorialMedico();
            if (historial == null) {
                return new CodigoResponse<>(404, "El apiario no tiene historial médico", null);
            }

            // Obtener todas las recetas del historial (recetas cumplidas)
            List<HistorialRecetas> historialRecetas = historialRecetasRepository.findByHistorialMedico(historial);

            // Cargar información completa de cada receta cumplida
            List<Receta> recetasCumplidas = new ArrayList<>();
            for (HistorialRecetas hr : historialRecetas) {
                Receta receta = hr.getReceta();

                // Cargar información de medicamentos para cada receta
                if (receta.getMedicamentos() != null) {
                    for (RecetaMedicamento rm : receta.getMedicamentos()) {
                        try {
                            MedicamentosResponse medicamentoInfo = microServiceClientMedicamentos
                                    .obtenerPorId(rm.getIdMedicamento());
                            rm.setMedicamentoInfo(medicamentoInfo);
                        } catch (Exception e) {
                            log.warn("No se pudo cargar información del medicamento ID: {}", rm.getIdMedicamento());
                            rm.setMedicamentoInfo(null);
                        }
                    }
                }
                recetasCumplidas.add(receta);
            }

            // Crear respuesta estructurada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("apiario", apiario);
            respuesta.put("historialMedico", historial);
            respuesta.put("recetasCumplidas", recetasCumplidas);
            respuesta.put("totalRecetasCumplidas", recetasCumplidas.size());
            respuesta.put("fechaConsulta", java.time.LocalDateTime.now());

            return new CodigoResponse<>(200, "Historial médico obtenido exitosamente", respuesta);

        } catch (Exception e) {
            log.error("Error al obtener historial completo: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno al obtener historial", null);
        }
    }

    // 🔍 Obtener todos los apiarios
    public CodigoResponse obtenerTodos() {
        List<Apiarios> apiarios = apiariosRepository.findAll();
        return new CodigoResponse<>(200, "Apiarios obtenidos exitosamente", apiarios);
    }

    // 🔍 Obtener apiario por ID con información completa de medicamentos
    public CodigoResponse obtenerPorId(Long id) {
        Apiarios apiario = apiariosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + id));

        // Cargar información de medicamentos si tiene receta
        if (apiario.getReceta() != null && apiario.getReceta().getMedicamentos() != null) {
            for (RecetaMedicamento rm : apiario.getReceta().getMedicamentos()) {
                try {
                    MedicamentosResponse medicamentoInfo = microServiceClientMedicamentos
                            .obtenerPorId(rm.getIdMedicamento());
                    rm.setMedicamentoInfo(medicamentoInfo);
                } catch (Exception e) {
                    log.warn("No se pudo cargar información del medicamento ID: {}", rm.getIdMedicamento());
                    rm.setMedicamentoInfo(null);
                }
            }
        }

        return new CodigoResponse<>(200, "Apiario obtenido exitosamente", apiario);
    }
}
