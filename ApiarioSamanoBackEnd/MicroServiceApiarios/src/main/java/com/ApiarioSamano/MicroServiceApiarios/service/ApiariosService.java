package com.ApiarioSamano.MicroServiceApiarios.service;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.HistorialMedicoDTO.HistorialMedicoDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO.RecetaRequest;
import com.ApiarioSamano.MicroServiceApiarios.factory.ApiariosFactory;
import com.ApiarioSamano.MicroServiceApiarios.factory.HistorialFactory;
import com.ApiarioSamano.MicroServiceApiarios.factory.RecetaFactory;
import com.ApiarioSamano.MicroServiceApiarios.model.*;
import com.ApiarioSamano.MicroServiceApiarios.repository.*;
import com.ApiarioSamano.MicroServiceApiarios.service.MicroServicesAPI.MedicamentosServiceClient.IMedicamentosService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class ApiariosService {

    private final ApiariosRepository apiariosRepository;
    private final RecetaRepository recetaRepository;
    private final HistorialMedicoRepository historialMedicoRepository;
    private final HistorialRecetasRepository historialRecetasRepository;
    private final RecetaMedicamentoRepository recetaMedicamentoRepository;

    private final ApiariosFactory apiariosFactory;
    private final RecetaFactory recetaFactory;
    private final HistorialFactory historialFactory;

    @Autowired
    private IMedicamentosService medicamentosService;

    // ==========================================================
    // 🔵 Crear APIARIO
    // ==========================================================
    @Transactional
    public CodigoResponse<Apiarios> crearApiario(ApiarioRequestDTO apiarioDTO) {
        try {
            log.info("🔵 Iniciando creación de apiario: {}", apiarioDTO);

            // Validar datos de entrada
            if (apiarioDTO.getNumeroApiario() == null) {
                throw new RuntimeException("numeroApiario es requerido");
            }
            if (apiarioDTO.getUbicacion() == null || apiarioDTO.getUbicacion().trim().isEmpty()) {
                throw new RuntimeException("ubicacion es requerido");
            }
            if (apiarioDTO.getSalud() == null || apiarioDTO.getSalud().trim().isEmpty()) {
                throw new RuntimeException("salud es requerido");
            }

            log.info("✅ Validaciones pasadas");

            // Crear apiario con factory
            Apiarios apiario = apiariosFactory.crear(apiarioDTO);

            // Establecer fecha de vinculación si hay dispositivo
            if (apiarioDTO.getDispositivoId() != null && !apiarioDTO.getDispositivoId().isEmpty()) {
                apiario.setFechaVinculacion(LocalDateTime.now());
                log.info("📱 Dispositivo vinculado: {}", apiarioDTO.getDispositivoId());
            }

            log.info("✅ Factory creó el objeto: {}", apiario);

            // Guardar apiario
            Apiarios apiarioGuardado = apiariosRepository.save(apiario);
            log.info("✅ Apiario guardado en BD: {}", apiarioGuardado);

            return new CodigoResponse<>(200, "Apiario creado exitosamente", apiarioGuardado);

        } catch (Exception e) {
            log.error("❌ ERROR al crear apiario: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 🟡 Modificar APIARIO
    // ==========================================================
    @Transactional
    public CodigoResponse<Apiarios> modificarApiario(Long id, ApiarioRequestDTO datosActualizados) {
        try {
            Apiarios apiario = apiariosRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + id));

            log.info("🔵 Modificando apiario ID: {}", id);

            // Guardar dispositivo anterior para comparar
            String dispositivoAnterior = apiario.getDispositivoId();
            String dispositivoNuevo = datosActualizados.getDispositivoId();

            // Actualizar datos básicos
            apiario.setNumeroApiario(datosActualizados.getNumeroApiario());
            apiario.setUbicacion(datosActualizados.getUbicacion());
            apiario.setSalud(datosActualizados.getSalud());
            apiario.setDispositivoId(dispositivoNuevo);

            // Actualizar fecha de vinculación si cambia el dispositivo
            if (!Objects.equals(dispositivoAnterior, dispositivoNuevo)) {
                if (dispositivoNuevo != null && !dispositivoNuevo.isEmpty()) {
                    apiario.setFechaVinculacion(LocalDateTime.now());
                    log.info("📱 Nuevo dispositivo vinculado: {}", dispositivoNuevo);
                } else {
                    apiario.setFechaVinculacion(null);
                    log.info("📱 Dispositivo removido");
                }
            }

            Apiarios apiarioActualizado = apiariosRepository.save(apiario);
            log.info("✅ Apiario actualizado: {}", apiarioActualizado);

            return new CodigoResponse<>(200, "Apiario actualizado correctamente", apiarioActualizado);

        } catch (Exception e) {
            log.error("❌ ERROR al modificar apiario: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 🔴 Eliminar apiario
    // ==========================================================
    @Transactional
    public CodigoResponse<Void> eliminarApiario(Long id) {
        try {
            Apiarios apiario = apiariosRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + id));

            log.info("🔴 Eliminando apiario ID: {}", id);

            // Desvincular historial médico antes de eliminar
            apiario.setHistorialMedico(null);
            apiariosRepository.delete(apiario);

            log.info("✅ Apiario eliminado correctamente");
            return new CodigoResponse<>(200, "Apiario eliminado correctamente", null);

        } catch (Exception e) {
            log.error("❌ ERROR al eliminar apiario: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 🟣 Agregar receta
    // ==========================================================
    @Transactional
    public CodigoResponse<Receta> agregarReceta(Long idApiario, RecetaRequest recetaDTO) {
        try {
            log.info("🔵 Iniciando creación de receta para apiario {}", idApiario);

            Apiarios apiario = apiariosRepository.findById(idApiario)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + idApiario));

            // Crear receta con factory
            Receta receta = recetaFactory.crearReceta(recetaDTO);

            // Guardar receta
            Receta recetaGuardada = recetaRepository.save(receta);
            log.info("✅ Receta guardada: ID={}", recetaGuardada.getId());

            // Crear medicamentos
            if (recetaDTO.getMedicamentos() != null && !recetaDTO.getMedicamentos().isEmpty()) {
                List<RecetaMedicamento> lista = recetaDTO.getMedicamentos()
                        .stream()
                        .map(med -> {
                            log.debug("🔍 Obteniendo información del medicamento ID: {}", med.getId());
                            MedicamentosResponse info = medicamentosService.obtenerPorId(med.getId());

                            RecetaMedicamento rm = recetaFactory.crearRecetaMedicamento(med.getId(), info);
                            rm.setReceta(recetaGuardada);
                            return rm;
                        })
                        .toList();

                List<RecetaMedicamento> guardados = recetaMedicamentoRepository.saveAll(lista);
                recetaGuardada.setMedicamentos(guardados);
                recetaRepository.save(recetaGuardada);
                log.info("✅ {} medicamentos agregados a la receta", guardados.size());
            }

            // Crear historial médico si no existe
            HistorialMedico historial = apiario.getHistorialMedico();
            if (historial == null) {
                log.info("📋 Creando historial médico para apiario {}", idApiario);
                HistorialMedicoDTO dto = new HistorialMedicoDTO("Historial creado automáticamente");
                historial = historialFactory.crearHistorialMedico(dto);
                historial = historialMedicoRepository.save(historial);

                apiario.setHistorialMedico(historial);
                apiariosRepository.save(apiario);
                log.info("✅ Historial médico creado: ID={}", historial.getId());
            }

            // Asociar receta activa al apiario
            apiario.setReceta(recetaGuardada);
            apiariosRepository.save(apiario);

            log.info("✅ Receta agregada correctamente al apiario {}", idApiario);
            return new CodigoResponse<>(200, "Receta agregada correctamente", recetaGuardada);

        } catch (Exception e) {
            log.error("❌ ERROR al agregar receta: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 🔥 Marcar receta como cumplida - CORREGIDO
    // ==========================================================

    // ==========================================================
    // 🔥 Marcar receta como cumplida - GUARDAR EN NOTAS
    // ==========================================================
    @Transactional
    public CodigoResponse<Apiarios> eliminarRecetaCumplida(Long idApiario) {
        try {
            log.info("🔥 Marcando receta como cumplida para apiario ID: {}", idApiario);

            // 1. Buscar el apiario
            Apiarios apiario = apiariosRepository.findById(idApiario)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado"));

            // 2. Verificar si tiene receta asignada
            Receta receta = apiario.getReceta();
            if (receta == null) {
                log.warn("⚠️ El apiario {} no tiene receta asignada", idApiario);
                return new CodigoResponse<>(400, "El apiario no tiene receta asignada", null);
            }

            log.info("📋 Receta encontrada: ID={}, Descripción={}", receta.getId(), receta.getDescripcion());

            // 3. Obtener o crear historial médico
            HistorialMedico historial = apiario.getHistorialMedico();
            if (historial == null) {
                log.info("📋 Creando nuevo historial médico para apiario {}", idApiario);
                HistorialMedicoDTO dto = new HistorialMedicoDTO("Historial creado al marcar receta como cumplida");
                historial = historialFactory.crearHistorialMedico(dto);
                historial = historialMedicoRepository.save(historial);

                apiario.setHistorialMedico(historial);
                apiario = apiariosRepository.save(apiario);
                log.info("✅ Historial médico creado: ID={}", historial.getId());
            } else {
                log.info("📋 Historial médico ya existe: ID={}", historial.getId());
            }

            // 4. Construir la información de la receta para agregar a notas
            StringBuilder recetaInfo = new StringBuilder();

            // Fecha actual
            recetaInfo.append("[")
                    .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .append("] ");

            // Descripción de la receta
            String descripcionOriginal = receta.getDescripcion().replace("CUMPLIDA - ", "");
            recetaInfo.append("RECETA CUMPLIDA: ").append(descripcionOriginal);

            // Medicamentos si existen
            if (receta.getMedicamentos() != null && !receta.getMedicamentos().isEmpty()) {
                recetaInfo.append(" - Medicamentos: ");
                for (RecetaMedicamento rm : receta.getMedicamentos()) {
                    try {
                        MedicamentosResponse info = medicamentosService.obtenerPorId(rm.getIdMedicamento());
                        recetaInfo.append(info.getNombre()).append(", ");
                    } catch (Exception e) {
                        recetaInfo.append("Med-ID:").append(rm.getIdMedicamento()).append(", ");
                    }
                }
                // Eliminar última coma
                if (recetaInfo.toString().endsWith(", ")) {
                    recetaInfo.setLength(recetaInfo.length() - 2);
                }
            }

            // 5. Agregar la receta a las notas del historial
            String notasActuales = historial.getNotas();
            String nuevaNota = recetaInfo.toString();

            if (notasActuales == null || notasActuales.trim().isEmpty()) {
                // Si no hay notas, crear la primera
                historial.setNotas(nuevaNota);
            } else {
                // Si ya hay notas, agregar la nueva con separador
                historial.setNotas(notasActuales + "\n---\n" + nuevaNota);
            }

            // Guardar el historial actualizado
            historial = historialMedicoRepository.save(historial);
            log.info("✅ Receta agregada a notas del historial: {}", nuevaNota);

            // 6. Marcar receta como cumplida en su descripción
            if (!descripcionOriginal.startsWith("CUMPLIDA - ")) {
                receta.setDescripcion("CUMPLIDA - " + descripcionOriginal);
                receta = recetaRepository.save(receta);
                log.info("🏷️ Receta marcada como CUMPLIDA: {}", receta.getDescripcion());
            }

            // 7. Desvincular receta activa del apiario
            apiario.setReceta(null);
            Apiarios apiarioActualizado = apiariosRepository.save(apiario);
            log.info("✅ Receta desvinculada del apiario");

            // 8. VERIFICACIÓN FINAL
            log.info("🔍 VERIFICACIÓN FINAL: Notas actualizadas en historial ID: {}", historial.getId());
            log.info("📝 Contenido de notas:\n{}", historial.getNotas());

            log.info("🎉 Receta marcada como cumplida exitosamente para apiario {}", idApiario);
            return new CodigoResponse<>(200, "Receta cumplida y guardada en notas del historial", apiarioActualizado);

        } catch (Exception e) {
            log.error("❌ ERROR al marcar receta como cumplida: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 🔍 Obtener todos los apiarios
    // ==========================================================
    public CodigoResponse<List<Apiarios>> obtenerTodos() {
        try {
            List<Apiarios> apiarios = apiariosRepository.findAll();
            log.info("✅ Obtenidos {} apiarios", apiarios.size());
            return new CodigoResponse<>(200, "Apiarios obtenidos", apiarios);
        } catch (Exception e) {
            log.error("❌ ERROR al obtener apiarios: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 🔍 Obtener apiario por ID
    // ==========================================================
    public CodigoResponse<Apiarios> obtenerPorId(Long id) {
        try {
            Apiarios apiario = apiariosRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado"));

            // Cargar información de medicamentos
            if (apiario.getReceta() != null && apiario.getReceta().getMedicamentos() != null) {
                for (RecetaMedicamento rm : apiario.getReceta().getMedicamentos()) {
                    try {
                        log.debug("🔍 Cargando medicamento ID: {} para apiario", rm.getIdMedicamento());
                        rm.setMedicamentoInfo(medicamentosService.obtenerPorId(rm.getIdMedicamento()));
                    } catch (Exception ignored) {
                        rm.setMedicamentoInfo(null);
                    }
                }
            }

            log.info("✅ Apiario encontrado: {}", apiario.getId());
            return new CodigoResponse<>(200, "Apiario encontrado", apiario);

        } catch (Exception e) {
            log.error("❌ ERROR al obtener apiario: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 🔍 Obtener historial médico por ID
    // ==========================================================
    public CodigoResponse<Map<String, Object>> obtenerHistorialMedicoPorId(Long idHistorial) {
        try {
            HistorialMedico historial = historialMedicoRepository.findById(idHistorial)
                    .orElseThrow(() -> new RuntimeException("Historial médico no encontrado con ID: " + idHistorial));

            List<HistorialRecetas> relaciones = historialRecetasRepository.findByHistorialMedico(historial);
            List<Receta> recetas = new ArrayList<>();

            for (HistorialRecetas hr : relaciones) {
                Receta r = hr.getReceta();

                // Cargar info del medicamento
                if (r.getMedicamentos() != null) {
                    for (RecetaMedicamento rm : r.getMedicamentos()) {
                        try {
                            log.debug("🔍 Cargando medicamento ID: {} para historial", rm.getIdMedicamento());
                            rm.setMedicamentoInfo(medicamentosService.obtenerPorId(rm.getIdMedicamento()));
                        } catch (Exception ignored) {
                            rm.setMedicamentoInfo(null);
                        }
                    }
                }
                recetas.add(r);
            }

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("historialMedico", historial);
            respuesta.put("recetas", recetas);
            respuesta.put("totalRecetas", recetas.size());

            return new CodigoResponse<>(200, "Historial obtenido", respuesta);

        } catch (Exception e) {
            log.error("❌ ERROR al obtener historial médico: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 🔍 Obtener historial completo del apiario
    // ==========================================================
    public CodigoResponse<Map<String, Object>> obtenerHistorialCompletoApiario(Long idApiario) {
        try {
            Apiarios apiario = apiariosRepository.findById(idApiario)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + idApiario));

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("apiario", apiario);

            HistorialMedico historial = apiario.getHistorialMedico();

            if (historial == null) {
                respuesta.put("historialMedico", "Este apiario no tiene historial médico");
                respuesta.put("totalRecetas", 0);
                respuesta.put("recetas", Collections.emptyList());
                return new CodigoResponse<>(200, "Historial vacío", respuesta);
            }

            respuesta.put("historialMedico", historial);

            List<HistorialRecetas> relaciones = historialRecetasRepository.findByHistorialMedico(historial);
            List<Receta> recetas = new ArrayList<>();

            for (HistorialRecetas hr : relaciones) {
                Receta receta = hr.getReceta();

                // Cargar información de medicamentos
                if (receta.getMedicamentos() != null) {
                    for (RecetaMedicamento rm : receta.getMedicamentos()) {
                        try {
                            log.debug("🔍 Cargando medicamento ID: {} para historial completo", rm.getIdMedicamento());
                            rm.setMedicamentoInfo(medicamentosService.obtenerPorId(rm.getIdMedicamento()));
                        } catch (Exception ignored) {
                            rm.setMedicamentoInfo(null);
                        }
                    }
                }
                recetas.add(receta);
            }

            respuesta.put("recetas", recetas);
            respuesta.put("totalRecetas", recetas.size());

            // Cargar receta activa
            Receta recetaActiva = apiario.getReceta();
            if (recetaActiva != null && recetaActiva.getMedicamentos() != null) {
                for (RecetaMedicamento rm : recetaActiva.getMedicamentos()) {
                    try {
                        log.debug("🔍 Cargando medicamento ID: {} para receta activa", rm.getIdMedicamento());
                        rm.setMedicamentoInfo(medicamentosService.obtenerPorId(rm.getIdMedicamento()));
                    } catch (Exception ignored) {
                        rm.setMedicamentoInfo(null);
                    }
                }
            }
            respuesta.put("recetaActiva", recetaActiva);

            log.info("✅ Historial completo obtenido para apiario {}", idApiario);
            return new CodigoResponse<>(200, "Historial completo obtenido correctamente", respuesta);

        } catch (Exception e) {
            log.error("❌ ERROR al obtener historial completo: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }
}