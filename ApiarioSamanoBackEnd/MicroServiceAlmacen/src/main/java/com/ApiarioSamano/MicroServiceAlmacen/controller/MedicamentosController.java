package com.ApiarioSamano.MicroServiceAlmacen.controller;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamentosRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamentosResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamnetosConProveedorResponse;
import com.ApiarioSamano.MicroServiceAlmacen.services.MedicamentosService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
@CrossOrigin(origins = "*")
public class MedicamentosController {

    @Autowired
    private MedicamentosService medicamentosService;

    // ========================
    // 📌 CREAR O ACTUALIZAR
    // ========================
    @PostMapping("/crear")
    public CodigoResponse<MedicamentosResponse> crearMedicamento(@RequestBody MedicamentosRequest request) {
        MedicamentosResponse nuevo = medicamentosService.guardar(request);
        return new CodigoResponse<>(200, "Medicamento guardado correctamente", nuevo);
    }

    // ========================
    // 📌 OBTENER POR ID
    // ========================
    @GetMapping("/{id}")
    public CodigoResponse<MedicamentosResponse> obtenerPorId(@PathVariable Long id) {
        MedicamentosResponse medicamento = medicamentosService.obtenerPorId(id);
        return new CodigoResponse<>(200, "Medicamento obtenido correctamente", medicamento);
    }

    // ========================
    // 📌 OBTENER TODOS
    // ========================
    @GetMapping("/todos")
    public CodigoResponse<List<MedicamentosResponse>> obtenerTodos() {
        List<MedicamentosResponse> lista = medicamentosService.obtenerTodos();
        return new CodigoResponse<>(200, "Lista de medicamentos obtenida correctamente", lista);
    }

    // ==============================
    // 📌 OBTENER TODOS CON PROVEEDOR
    // ==============================
    @GetMapping("/todos-con-proveedor")
    public CodigoResponse<List<MedicamnetosConProveedorResponse>> obtenerTodosConProveedor() {
        List<MedicamnetosConProveedorResponse> lista = medicamentosService.obtenerTodosConProveedor();
        return new CodigoResponse<>(200, "Lista de medicamentos con proveedores obtenida correctamente", lista);
    }

    // ========================================
    // 📌 OBTENER POR ID DE PROVEEDOR (SIN DETALLE)
    // ========================================
    @GetMapping("/proveedor/{idProveedor}")
    public CodigoResponse<List<MedicamentosResponse>> obtenerPorProveedor(@PathVariable Integer idProveedor) {
        List<MedicamentosResponse> lista = medicamentosService.obtenerPorProveedor(idProveedor);
        return new CodigoResponse<>(200, "Medicamentos del proveedor " + idProveedor + " obtenidos correctamente",
                lista);
    }

    // ========================================
    // 📌 OBTENER POR ID DE PROVEEDOR (CON DETALLE)
    // ========================================
    @GetMapping("/proveedor-detalle/{idProveedor}")
    public CodigoResponse<List<MedicamnetosConProveedorResponse>> obtenerPorProveedorConDetalle(
            @PathVariable Integer idProveedor) {
        List<MedicamnetosConProveedorResponse> lista = medicamentosService.obtenerPorProveedorConDetalle(idProveedor);
        return new CodigoResponse<>(200,
                "Medicamentos del proveedor " + idProveedor + " con detalle obtenidos correctamente", lista);
    }

    // ========================
    // 📌 ELIMINAR POR ID
    // ========================
    @DeleteMapping("/eliminar/{id}")
    public CodigoResponse<Void> eliminar(@PathVariable Long id) {
        medicamentosService.eliminar(id);
        return new CodigoResponse<>(200, "Medicamento eliminado correctamente", null);
    }
}
