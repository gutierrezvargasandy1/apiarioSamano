package com.ApiarioSamano.MicroServiceProduccion.controller;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.CosechaDTO.CosechaRequest;
import com.ApiarioSamano.MicroServiceProduccion.model.Cosecha;
import com.ApiarioSamano.MicroServiceProduccion.services.CosechaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cosechas")
@RequiredArgsConstructor
public class CosechaController {

    private final CosechaService cosechaService;

    @PostMapping("/crear")
    public CodigoResponse<Cosecha> crearCosecha(@RequestBody CosechaRequest request) {
        return cosechaService.guardarCosecha(request);
    }

    @PutMapping("/{id}")
    public CodigoResponse<Cosecha> actualizarCosecha(
            @PathVariable Long id,
            @RequestBody CosechaRequest request) {
        return cosechaService.actualizarCosecha(id, request);
    }

    @GetMapping("/listar")
    public CodigoResponse<List<Cosecha>> listarCosechas() {
        return cosechaService.listarCosechas();
    }

    @GetMapping("/{id}")
    public CodigoResponse<Cosecha> obtenerCosechaPorId(@PathVariable Long id) {
        return cosechaService.obtenerPorId(id);
    }

    @GetMapping("/lote/{idLote}")
    public CodigoResponse<List<Cosecha>> obtenerCosechasPorLote(@PathVariable Long idLote) {
        return cosechaService.obtenerCosechasPorLote(idLote);
    }

    @GetMapping("/apiario/{idApiario}")
    public CodigoResponse<List<Cosecha>> obtenerCosechasPorApiario(@PathVariable Long idApiario) {
        return cosechaService.obtenerCosechasPorApiario(idApiario);
    }

    @GetMapping("/rango-fechas")
    public CodigoResponse<List<Cosecha>> obtenerCosechasPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return cosechaService.obtenerCosechasPorRangoFechas(fechaInicio, fechaFin);
    }

    @DeleteMapping("/{id}")
    public CodigoResponse<Void> eliminarCosecha(@PathVariable Long id) {
        return cosechaService.eliminarCosecha(id);
    }

}