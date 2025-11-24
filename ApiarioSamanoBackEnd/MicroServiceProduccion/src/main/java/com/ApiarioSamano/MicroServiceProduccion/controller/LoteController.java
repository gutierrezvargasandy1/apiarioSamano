package com.ApiarioSamano.MicroServiceProduccion.controller;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.LoteDTO.LoteConAlmacenResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.LoteDTO.LoteRequest;
import com.ApiarioSamano.MicroServiceProduccion.model.Lote;
import com.ApiarioSamano.MicroServiceProduccion.services.LoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;

    // 🔹 Crear o actualizar un lote
    @PostMapping("/crear")
    public CodigoResponse<Lote> guardarLote(@RequestBody LoteRequest request) {
        return loteService.guardarLote(request);
    }

    // 🔹 Listar todos los lotes
    @GetMapping
    public CodigoResponse<List<Lote>> listarLotes() {
        return loteService.listarLotes();
    }

    // 🔹 Obtener un lote por ID junto con su almacén
    @GetMapping("/{id}")
    public CodigoResponse<LoteConAlmacenResponse> obtenerLoteConAlmacen(@PathVariable Long id) {
        return loteService.obtenerLoteConAlmacenPorId(id);
    }

    // 🔹 Eliminar un lote por ID
    @DeleteMapping("/{id}")
    public CodigoResponse<Void> eliminarLote(@PathVariable Long id) {
        return loteService.eliminarLote(id);
    }
}
