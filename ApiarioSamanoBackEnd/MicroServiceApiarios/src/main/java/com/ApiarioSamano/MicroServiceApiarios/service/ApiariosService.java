package com.ApiarioSamano.MicroServiceApiarios.service;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.HistorialMedicoDTO.HistorialMedicoDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO.RecetaRequest;
import com.ApiarioSamano.MicroServiceApiarios.factory.ApairiosFactory;
import com.ApiarioSamano.MicroServiceApiarios.factory.HistorialMedicoFactory;
import com.ApiarioSamano.MicroServiceApiarios.factory.RecetaFactory;
import com.ApiarioSamano.MicroServiceApiarios.model.*;
import com.ApiarioSamano.MicroServiceApiarios.repository.*;
import com.ApiarioSamano.MicroServiceApiarios.service.MicroServicesAPI.MicroServiceClientMedicamentos;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private final ApairiosFactory apairiosFactory;
    private final RecetaFactory recetaFactory;
    private final HistorialMedicoFactory historialMedicoFactory;

    @Autowired
    private MicroServiceClientMedicamentos microServiceClientMedicamentos;

    // 🔵 Crear APIARIO usando FACTORY
    public CodigoResponse crearApiario(ApiarioRequestDTO apiarioDTO) {

        // 💠 USO CORRECTO DE Factory<Apiarios, ApiarioRequestDTO>
        Apiarios apiario = apairiosFactory.crear(apiarioDTO);

        apiariosRepository.save(apiario);
        log.info("✅ [CREAR APIARIO] Apiario creado con factory: {}", apiario);

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

        apiario.setHistorialMedico(null);
        apiariosRepository.delete(apiario);

        return new CodigoResponse<>(200, "Apiario eliminado correctamente", null);
    }

    // 🟣 Agregar receta usando FACTORY DE RECETA
    public CodigoResponse<Receta> agregarReceta(Long idApiario, RecetaRequest recetaDTO) {

        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Iniciando creación de receta para apiario {}", idApiario);

        Apiarios apiario = apiariosRepository.findById(idApiario)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado con ID: " + idApiario));

        // 💠 USO REAL DE Factory<Receta, RecetaRequest>
        Receta receta = recetaFactory.crear(recetaDTO);

        // Guardar la receta base
        Receta recetaGuardada = recetaRepository.save(receta);

        // ================================
        //   CARGAR MEDICAMENTOS REALES
        // ================================
        if (recetaDTO.getMedicamentos() != null && !recetaDTO.getMedicamentos().isEmpty()) {

            List<RecetaMedicamento> lista = recetaDTO.getMedicamentos().stream()
                    .map(med -> {
                        MedicamentosResponse info = microServiceClientMedicamentos.obtenerPorId(med.getId());

                        RecetaMedicamento rm = new RecetaMedicamento();
                        rm.setReceta(recetaGuardada);
                        rm.setIdMedicamento(info.getId());
                        rm.setMedicamentoInfo(info);

                        return rm;
                    }).toList();

            List<RecetaMedicamento> guardados = recetaMedicamentoRepository.saveAll(lista);

            recetaGuardada.setMedicamentos(guardados);
            recetaRepository.save(recetaGuardada);
        }

        // ============================
        //   HISTORIAL MÉDICO
        // ============================
        HistorialMedico historial = apiario.getHistorialMedico();

        if (historial == null) {
            // 💠 USAR FACTORY DE HISTORIAL
            historial = historialMedicoFactory.crear(
                    new HistorialMedicoDTO("Historial creado automáticamente")
            );
            historial = historialMedicoRepository.save(historial);
            apiario.setHistorialMedico(historial);
            apiariosRepository.save(apiario);
        }

        // Registrar receta en historial
        HistorialRecetas hr = new HistorialRecetas();
        hr.setHistorialMedico(historial);
        hr.setReceta(recetaGuardada);
        historialRecetasRepository.save(hr);

        // Asociar receta activa al apiario
        apiario.setReceta(recetaGuardada);
        apiariosRepository.save(apiario);

        Receta recetaCompleta = recetaRepository.findById(recetaGuardada.getId())
                .orElseThrow(() -> new RuntimeException("Error al cargar receta"));

        return new CodigoResponse<>(200, "Receta agregada correctamente", recetaCompleta);
    }

    // 🔍 Obtener historial médico por ID
    public CodigoResponse obtenerHistorialMedicoPorId(Long idHistorial) {
        HistorialMedico historial = historialMedicoRepository.findById(idHistorial)
                .orElseThrow(() -> new RuntimeException("Historial médico no encontrado con ID: " + idHistorial));

        List<HistorialRecetas> relaciones = historialRecetasRepository.findByHistorialMedico(historial);
        List<Receta> recetas = new ArrayList<>();

        for (HistorialRecetas hr : relaciones) {
            Receta r = hr.getReceta();

            if (r.getMedicamentos() != null) {
                for (RecetaMedicamento rm : r.getMedicamentos()) {
                    try {
                        rm.setMedicamentoInfo(
                                microServiceClientMedicamentos.obtenerPorId(rm.getIdMedicamento())
                        );
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
    }

    // 🔥 Marcar receta como cumplida
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
                historial = historialMedicoRepository.save(new HistorialMedico());
                apiario.setHistorialMedico(historial);
                apiariosRepository.save(apiario);
            }

            // Registrar receta cumplida
            HistorialRecetas hr = new HistorialRecetas();
            hr.setHistorialMedico(historial);
            hr.setReceta(receta);
            historialRecetasRepository.save(hr);

            // Marcar receta como cumplida
            receta.setDescripcion("CUMPLIDA - " + receta.getDescripcion());
            recetaRepository.save(receta);

            apiario.setReceta(null);
            apiariosRepository.save(apiario);

            return new CodigoResponse<>(200, "Receta cumplida correctamente", apiario);

        } catch (Exception e) {
            return new CodigoResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    // ----------------------------------------------------------
    // OTROS MÉTODOS
    // ----------------------------------------------------------

    public CodigoResponse obtenerTodos() {
        return new CodigoResponse<>(200, "Apiarios obtenidos", apiariosRepository.findAll());
    }

    public CodigoResponse obtenerPorId(Long id) {
        Apiarios apiario = apiariosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apiario no encontrado"));

        // Cargar medicamentos de receta activa
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

        return new CodigoResponse<>(200, "Apiario encontrado", apiario);
    }

    // 🔵 Obtener historial COMPLETO de un apiario (historial + recetas + medicamentos)
public CodigoResponse obtenerHistorialCompletoApiario(Long idApiario) {

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
                            microServiceClientMedicamentos.obtenerPorId(rm.getIdMedicamento())
                    );
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
    if (recetaActiva != null) {
        // cargar medicamentos
        if (recetaActiva.getMedicamentos() != null) {
            for (RecetaMedicamento rm : recetaActiva.getMedicamentos()) {
                try {
                    rm.setMedicamentoInfo(
                            microServiceClientMedicamentos.obtenerPorId(rm.getIdMedicamento())
                    );
                } catch (Exception ignored) {
                    rm.setMedicamentoInfo(null);
                }
            }
        }
    }

    respuesta.put("recetaActiva", recetaActiva);

    return new CodigoResponse<>(200, "Historial completo obtenido correctamente", respuesta);
}



}
