package com.ApiarioSamano.MicroServiceApiarios.controller;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO.RecetaRequest;
import com.ApiarioSamano.MicroServiceApiarios.model.Receta;
import com.ApiarioSamano.MicroServiceApiarios.model.Apiarios;
import com.ApiarioSamano.MicroServiceApiarios.service.ApiariosService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/apiarios")
@RequiredArgsConstructor
public class ApiariosController {

    private final ApiariosService apiariosService;

    // 🟢 Crear nuevo apiario
    @PostMapping
    public CodigoResponse<Apiarios> crearApiario(@RequestBody ApiarioRequestDTO apiarioDTO) {
        return apiariosService.crearApiario(apiarioDTO);
    }

    // 🟡 Modificar un apiario existente
    @PutMapping("/{id}")
    public CodigoResponse<Apiarios> modificarApiario(@PathVariable Long id,
            @RequestBody ApiarioRequestDTO datosActualizados) {
        return apiariosService.modificarApiario(id, datosActualizados);
    }

    // 🔴 Eliminar apiario
    @DeleteMapping("/{id}")
    public CodigoResponse<Void> eliminarApiario(@PathVariable Long id) {
        return apiariosService.eliminarApiario(id);
    }

    // 🔹 Agregar receta a un apiario
    @PostMapping("/{idApiario}/recetas")
    public CodigoResponse<Receta> agregarReceta(@PathVariable Long idApiario,
            @RequestBody RecetaRequest recetaDTO) {
        return apiariosService.agregarReceta(idApiario, recetaDTO);
    }

    // 🔹 Eliminar receta cumplida
    @DeleteMapping("/{idApiario}/receta")
    public ResponseEntity<CodigoResponse> eliminarRecetaCumplida(@PathVariable Long idApiario) {
        try {
            CodigoResponse response = apiariosService.eliminarRecetaCumplida(idApiario);
            return ResponseEntity.status(response.getCodigo()).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new CodigoResponse(500, "Error interno del servidor", null));
        }
    }

    // 🔍 Obtener todos los apiarios
    @GetMapping
    public CodigoResponse<List<Apiarios>> obtenerTodos() {
        return apiariosService.obtenerTodos();
    }

    @GetMapping("/{idApiario}/historial-completo")
    public ResponseEntity<CodigoResponse> obtenerHistorialCompleto(@PathVariable Long idApiario) {
        try {
            CodigoResponse response = apiariosService.obtenerHistorialCompletoApiario(idApiario);
            return ResponseEntity.status(response.getCodigo()).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new CodigoResponse(500, "Error interno del servidor", null));
        }
    }

    // 🔍 Obtener apiario por ID
    @GetMapping("/{id}")
    public CodigoResponse<Apiarios> obtenerPorId(@PathVariable Long id) {
        return apiariosService.obtenerPorId(id);
    }

    // 🔍 Obtener historial médico por ID con recetas y medicamentos completos
    @GetMapping("/historial-medico/{idHistorial}")
    public CodigoResponse obtenerHistorialMedicoPorId(@PathVariable Long idHistorial) {
        return apiariosService.obtenerHistorialMedicoPorId(idHistorial);
    }

}
