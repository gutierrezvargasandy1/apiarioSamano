package com.ApiarioSamano.MicroServiceApiarios.service;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.HistorialMedicoDTO.HistorialMedicoDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO.RecetaRequest;
import com.ApiarioSamano.MicroServiceApiarios.dto.ResetaMedicamentoDTO.RecetaMedicamentoDTO;
import com.ApiarioSamano.MicroServiceApiarios.factory.HistorialMedicoFactory;
import com.ApiarioSamano.MicroServiceApiarios.factory.HistorialRecetasFactory;
import com.ApiarioSamano.MicroServiceApiarios.factory.ApiariosFactory;
import com.ApiarioSamano.MicroServiceApiarios.factory.RecetaFactory;
import com.ApiarioSamano.MicroServiceApiarios.factory.RecetaMedicamentoFactory;
import com.ApiarioSamano.MicroServiceApiarios.model.*;
import com.ApiarioSamano.MicroServiceApiarios.repository.*;
import com.ApiarioSamano.MicroServiceApiarios.service.MicroServicesAPI.MicroServiceClientMedicamentos;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    // FACTORIES
    private final ApiariosFactory apairiosFactory;
    private final RecetaFactory recetaFactory;
    private final RecetaMedicamentoFactory recetaMedicamentoFactory;
    private final HistorialMedicoFactory historialMedicoFactory;
    private final HistorialRecetasFactory historialRecetasFactory;

    @Autowired
    private MicroServiceClientMedicamentos microServiceClientMedicamentos;

    // ==========================================================
    // 🔵 Crear APIARIO usando FACTORY - CORREGIDO
    // ==========================================================
    @Transactional
    public CodigoResponse crearApiario(ApiarioRequestDTO apiarioDTO) {
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

            // Crear apiario con factory (sin historial médico inicial)
            Apiarios apiario = apairiosFactory.crear(apiarioDTO);

            // 🔥 ESTABLECER FECHA DE VINCULACIÓN SI HAY DISPOSITIVO
            if (apiarioDTO.getDispositivoId() != null && !apiarioDTO.getDispositivoId().isEmpty()) {
                apiario.setFechaVinculacion(LocalDateTime.now());
                log.info("📱 Dispositivo vinculado: {}", apiarioDTO.getDispositivoId());
            }

            log.info("✅ Factory creó el objeto: {}", apiario);

            // Guardar apiario primero
            Apiarios apiarioGuardado = apiariosRepository.save(apiario);
            log.info("✅ Apiario guardado en BD: {}", apiarioGuardado);

            return new CodigoResponse<>(200, "Apiario creado exitosamente", apiarioGuardado);

        } catch (Exception e) {
            log.error("❌ ERROR al crear apiario: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 🟡 Modificar APIARIO - ACTUALIZADO CON FECHA DE VINCULACIÓN
    // ==========================================================
    @Transactional
    public CodigoResponse modificarApiario(Long id, ApiarioRequestDTO datosActualizados) {
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

            // 🔥 ACTUALIZAR FECHA DE VINCULACIÓN SI CAMBIA EL DISPOSITIVO
            if (!Objects.equals(dispositivoAnterior, dispositivoNuevo)) {
                if (dispositivoNuevo != null && !dispositivoNuevo.isEmpty()) {
                    // Si se asigna un nuevo dispositivo, actualizar fecha
                    apiario.setFechaVinculacion(LocalDateTime.now());
                    log.info("📱 Nuevo dispositivo vinculado: {}", dispositivoNuevo);
                } else {
                    // Si se remueve el dispositivo, limpiar fecha
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
    public CodigoResponse eliminarApiario(Long id) {
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
    // 🟣 Agregar receta usando TODAS las FACTORIES
    // ==========================================================
    @Transactional
    public CodigoResponse<Receta> agregarReceta(Long idApiario, RecetaRequest recetaDTO) {
        try {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.info("🔵 Iniciando creación de receta para apiario {}", idApiario);

            Apiarios apiario = apiariosRepository.findById(idApiario)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + idApiario));

            // Crear receta con factory
            Receta receta = recetaFactory.crear(recetaDTO);

            // Guardar receta
            Receta recetaGuardada = recetaRepository.save(receta);

            // ======================================
            // CREAR medicamentos con RecetaMedicamentoFactory
            // ======================================
            if (recetaDTO.getMedicamentos() != null && !recetaDTO.getMedicamentos().isEmpty()) {

                List<RecetaMedicamento> lista = recetaDTO.getMedicamentos()
                        .stream()
                        .map(med -> {

                            MedicamentosResponse info = microServiceClientMedicamentos.obtenerPorId(med.getId());

                            // AHORA sí se usa factory 👇
                            RecetaMedicamentoDTO dto = new RecetaMedicamentoDTO(
                                    info.getId(),
                                    info);

                            RecetaMedicamento rm = recetaMedicamentoFactory.crear(dto);

                            rm.setReceta(recetaGuardada);

                            return rm;
                        })
                        .toList();

                List<RecetaMedicamento> guardados = recetaMedicamentoRepository.saveAll(lista);

                recetaGuardada.setMedicamentos(guardados);
                recetaRepository.save(recetaGuardada);
            }

            // ======================================
            // HISTORIAL MÉDICO - CREAR SI NO EXISTE
            // ======================================
            HistorialMedico historial = apiario.getHistorialMedico();

            if (historial == null) {
                log.info("📋 Creando historial médico para apiario {}", idApiario);

                historial = historialMedicoFactory.crear(
                        new HistorialMedicoDTO("Historial creado automáticamente"));

                historial = historialMedicoRepository.save(historial);

                apiario.setHistorialMedico(historial);
                apiariosRepository.save(apiario);
            }

            // Registrar receta en historial usando FACTORY
            HistorialRecetas hr = historialRecetasFactory.crear(null);
            hr.setHistorialMedico(historial);
            hr.setReceta(recetaGuardada);
            historialRecetasRepository.save(hr);

            // Asociar receta activa
            apiario.setReceta(recetaGuardada);
            apiariosRepository.save(apiario);

            Receta recetaCompleta = recetaRepository.findById(recetaGuardada.getId())
                    .orElseThrow(() -> new RuntimeException("Error al cargar receta"));

            log.info("✅ Receta agregada correctamente al apiario {}", idApiario);
            return new CodigoResponse<>(200, "Receta agregada correctamente", recetaCompleta);

        } catch (Exception e) {
            log.error("❌ ERROR al agregar receta: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 🔍 Obtener historial médico
    // ==========================================================
    public CodigoResponse obtenerHistorialMedicoPorId(Long idHistorial) {
        try {
            HistorialMedico historial = historialMedicoRepository.findById(idHistorial)
                    .orElseThrow(() -> new RuntimeException("Historial médico no encontrado con ID: " + idHistorial));

            List<HistorialRecetas> relaciones = historialRecetasRepository.findByHistorialMedico(historial);
            List<Receta> recetas = new ArrayList<>();

            for (HistorialRecetas hr : relaciones) {

                Receta r = hr.getReceta();

                // cargar info del microservicio
                if (r.getMedicamentos() != null) {
                    for (RecetaMedicamento rm : r.getMedicamentos()) {
                        try {
                            rm.setMedicamentoInfo(
                                    microServiceClientMedicamentos.obtenerPorId(rm.getIdMedicamento()));
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
    // 🔥 Marcar receta como cumplida usando HISTORIAL FACTORY
    // ==========================================================
    @Transactional
    public CodigoResponse eliminarRecetaCumplida(Long idApiario) {
        try {
            Apiarios apiario = apiariosRepository.findById(idApiario)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado"));

            Receta receta = apiario.getReceta();
            if (receta == null) {
                return new CodigoResponse<>(400, "El apiario no tiene receta asignada", null);
            }

            HistorialMedico historial = apiario.getHistorialMedico();

            if (historial == null) {
                historial = historialMedicoFactory.crear(
                        new HistorialMedicoDTO("Historial creado automáticamente"));
                historial = historialMedicoRepository.save(historial);
                apiario.setHistorialMedico(historial);
                apiariosRepository.save(apiario);
            }

            // REGISTRAR RECETA CUMPLIDA usando FACTORY correctamente
            HistorialRecetas hr = historialRecetasFactory.crear(null);
            hr.setHistorialMedico(historial);
            hr.setReceta(receta);
            historialRecetasRepository.save(hr);

            // Marcar receta como cumplida
            receta.setDescripcion("CUMPLIDA - " + receta.getDescripcion());
            recetaRepository.save(receta);

            apiario.setReceta(null);
            apiariosRepository.save(apiario);

            log.info("✅ Receta marcada como cumplida para apiario {}", idApiario);
            return new CodigoResponse<>(200, "Receta cumplida correctamente", apiario);

        } catch (Exception e) {
            log.error("❌ ERROR al marcar receta como cumplida: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // OTROS MÉTODOS
    // ==========================================================
    public CodigoResponse obtenerTodos() {
        try {
            List<Apiarios> apiarios = apiariosRepository.findAll();
            log.info("✅ Obtenidos {} apiarios", apiarios.size());
            return new CodigoResponse<>(200, "Apiarios obtenidos", apiarios);
        } catch (Exception e) {
            log.error("❌ ERROR al obtener apiarios: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    public CodigoResponse obtenerPorId(Long id) {
        try {
            Apiarios apiario = apiariosRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Apiario no encontrado"));

            if (apiario.getReceta() != null && apiario.getReceta().getMedicamentos() != null) {
                for (RecetaMedicamento rm : apiario.getReceta().getMedicamentos()) {
                    try {
                        rm.setMedicamentoInfo(
                                microServiceClientMedicamentos.obtenerPorId(rm.getIdMedicamento()));
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

    public CodigoResponse obtenerHistorialCompletoApiario(Long idApiario) {
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

                if (receta.getMedicamentos() != null) {
                    for (RecetaMedicamento rm : receta.getMedicamentos()) {
                        try {
                            rm.setMedicamentoInfo(
                                    microServiceClientMedicamentos.obtenerPorId(rm.getIdMedicamento()));
                        } catch (Exception ignored) {
                            rm.setMedicamentoInfo(null);
                        }
                    }
                }

                recetas.add(receta);
            }

            respuesta.put("recetas", recetas);
            respuesta.put("totalRecetas", recetas.size());

            Receta recetaActiva = apiario.getReceta();

            if (recetaActiva != null && recetaActiva.getMedicamentos() != null) {
                for (RecetaMedicamento rm : recetaActiva.getMedicamentos()) {
                    try {
                        rm.setMedicamentoInfo(
                                microServiceClientMedicamentos.obtenerPorId(rm.getIdMedicamento()));
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