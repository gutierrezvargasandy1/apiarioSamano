package com.ApiarioSamano.MicroServiceProveedores.controller;

import com.ApiarioSamano.MicroServiceProveedores.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorDTO.ProveedorRequest;
import com.ApiarioSamano.MicroServiceProveedores.model.Proveedor;
import com.ApiarioSamano.MicroServiceProveedores.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<CodigoResponse<List<Proveedor>>> obtenerTodos() {
        CodigoResponse<List<Proveedor>> response = proveedorService.obtenerTodos();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodigoResponse<Proveedor>> obtenerPorId(@PathVariable Long id) {
        CodigoResponse<Proveedor> response = proveedorService.obtenerPorId(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping
    public ResponseEntity<CodigoResponse<Proveedor>> crearProveedor(@RequestBody ProveedorRequest request) {
        CodigoResponse<Proveedor> response = proveedorService.guardarProveedor(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CodigoResponse<Proveedor>> actualizarProveedor(@PathVariable Long id,
            @RequestBody ProveedorRequest request) {
        CodigoResponse<Proveedor> response = proveedorService.actualizarProveedor(id, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CodigoResponse<Void>> eliminarProveedor(@PathVariable Long id) {
        CodigoResponse<Void> response = proveedorService.eliminarProveedor(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}