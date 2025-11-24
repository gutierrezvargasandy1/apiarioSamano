package com.ApiarioSamano.MicroServiceAlmacen.controller;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.AlmacenRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.AlmacenResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO.ReporteEspaciosResponse;
import com.ApiarioSamano.MicroServiceAlmacen.services.AlmacenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/almacenes")
public class AlmacenController {

    @Autowired
    private AlmacenService almacenService;

    // Crear o actualizar un almacén
    @PostMapping("/crear")
    public CodigoResponse<AlmacenResponse> guardarAlmacen(@RequestBody AlmacenRequest request) {
        return almacenService.guardarAlmacen(request);
    }

    // Obtener todos los almacenes
    @GetMapping
    public CodigoResponse<List<AlmacenResponse>> obtenerTodos() {
        return almacenService.obtenerTodos();
    }

    // En AlmacenController
    @PutMapping("/{idAlmacen}/actualizar-espacios")
    public ResponseEntity<CodigoResponse<AlmacenResponse>> actualizarEspaciosOcupados(@PathVariable Long idAlmacen) {
        return ResponseEntity.ok(almacenService.actualizarEspaciosOcupadosAutomaticamente(idAlmacen));
    }

    @GetMapping("/{idAlmacen}/reporte-espacios")
    public ResponseEntity<CodigoResponse<ReporteEspaciosResponse>> obtenerReporteEspacios(
            @PathVariable Long idAlmacen) {
        return ResponseEntity.ok(almacenService.obtenerReporteEspaciosOcupados(idAlmacen));
    }

    // Obtener un almacén por ID
    @GetMapping("/{id}")
    public CodigoResponse<AlmacenResponse> obtenerPorId(@PathVariable Long id) {
        return almacenService.obtenerPorId(id);
    }

    // Eliminar un almacén por ID
    @DeleteMapping("/{id}")
    public CodigoResponse<Void> eliminarPorId(@PathVariable Long id) {
        return almacenService.eliminarPorId(id);
    }
}
