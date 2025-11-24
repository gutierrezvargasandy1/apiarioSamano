package com.ApiarioSamano.MicroServiceAlmacen.controller;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasConProveedorResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.services.HerramientasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/herramientas")
@RequiredArgsConstructor
public class HerramientasController {

    private final HerramientasService herramientasService;

    @PostMapping("/crear")
    public ResponseEntity<CodigoResponse<HerramientasResponse>> guardar(@RequestBody HerramientasRequest request) {
        try {
            log.info("📥 Request recibido para crear herramienta: {}", request);
            CodigoResponse<HerramientasResponse> response = herramientasService.guardar(request);
            log.info("✅ Herramienta creada exitosamente");
            return ResponseEntity.status(response.getCodigo()).body(response);
        } catch (DataIntegrityViolationException e) {
            log.error("❌ Error de integridad de datos al guardar herramienta", e);
            return ResponseEntity.status(400)
                    .body(new CodigoResponse<>(400, "Error de integridad: " + e.getMostSpecificCause().getMessage(),
                            null));
        } catch (RuntimeException e) {
            log.error("❌ Error en tiempo de ejecución al guardar herramienta", e);
            return ResponseEntity.status(500)
                    .body(new CodigoResponse<>(500, "Error: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Error inesperado al guardar herramienta", e);
            return ResponseEntity.status(500)
                    .body(new CodigoResponse<>(500, "Error inesperado: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<CodigoResponse<List<HerramientasResponse>>> obtenerTodas() {
        try {
            log.info("📋 Obteniendo todas las herramientas");
            CodigoResponse<List<HerramientasResponse>> response = herramientasService.obtenerTodas();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error al obtener herramientas", e);
            return ResponseEntity.status(500)
                    .body(new CodigoResponse<>(500, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodigoResponse<HerramientasResponse>> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("🔍 Buscando herramienta con ID: {}", id);
            CodigoResponse<HerramientasResponse> response = herramientasService.obtenerPorId(id);
            return ResponseEntity.status(response.getCodigo()).body(response);
        } catch (Exception e) {
            log.error("❌ Error al obtener herramienta con ID: {}", id, e);
            return ResponseEntity.status(500)
                    .body(new CodigoResponse<>(500, "Error: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CodigoResponse<Void>> eliminar(@PathVariable Long id) {
        try {
            log.info("🗑️ Eliminando herramienta con ID: {}", id);
            CodigoResponse<Void> response = herramientasService.eliminar(id);
            log.info("✅ Herramienta eliminada exitosamente");
            return ResponseEntity.status(response.getCodigo()).body(response);
        } catch (Exception e) {
            log.error("❌ Error al eliminar herramienta con ID: {}", id, e);
            return ResponseEntity.status(500)
                    .body(new CodigoResponse<>(500, "Error: " + e.getMessage(), null));
        }
    }

    // ================== MÉTODOS CON PROVEEDOR ==================

    @GetMapping("/con-proveedor")
    public ResponseEntity<CodigoResponse<List<HerramientasConProveedorResponse>>> obtenerTodasConProveedor() {
        try {
            log.info("📋 Obteniendo herramientas con información de proveedor");
            CodigoResponse<List<HerramientasConProveedorResponse>> response = herramientasService
                    .obtenerTodasConProveedor();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error al obtener herramientas con proveedor", e);
            return ResponseEntity.status(500)
                    .body(new CodigoResponse<>(500, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/con-proveedor/{id}")
    public ResponseEntity<CodigoResponse<HerramientasConProveedorResponse>> obtenerPorIdConProveedor(
            @PathVariable Long id) {
        try {
            log.info("🔍 Buscando herramienta con proveedor, ID: {}", id);
            CodigoResponse<HerramientasConProveedorResponse> response = herramientasService
                    .obtenerPorIdConProveedor(id);
            return ResponseEntity.status(response.getCodigo()).body(response);
        } catch (Exception e) {
            log.error("❌ Error al obtener herramienta con proveedor, ID: {}", id, e);
            return ResponseEntity.status(500)
                    .body(new CodigoResponse<>(500, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/proveedor/{idProveedor}")
    public ResponseEntity<CodigoResponse<List<HerramientasResponse>>> obtenerPorProveedor(
            @PathVariable Integer idProveedor) {
        try {
            log.info("🔍 Buscando herramientas del proveedor ID: {}", idProveedor);
            CodigoResponse<List<HerramientasResponse>> response = herramientasService.obtenerPorProveedor(idProveedor);
            return ResponseEntity.status(response.getCodigo()).body(response);
        } catch (Exception e) {
            log.error("❌ Error al obtener herramientas del proveedor ID: {}", idProveedor, e);
            return ResponseEntity.status(500)
                    .body(new CodigoResponse<>(500, "Error: " + e.getMessage(), null));
        }
    }
}