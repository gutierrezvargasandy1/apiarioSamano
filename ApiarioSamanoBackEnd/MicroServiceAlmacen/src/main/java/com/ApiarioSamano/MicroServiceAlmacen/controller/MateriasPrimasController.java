package com.ApiarioSamano.MicroServiceAlmacen.controller;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasConProveedorDTO;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.services.MateriasPrimasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materias-primas")
@CrossOrigin(origins = "*")
public class MateriasPrimasController {

    @Autowired
    private MateriasPrimasService materiasPrimasService;

    // Crear o actualizar
    @PostMapping("/crear")
    public CodigoResponse<MateriasPrimasResponse> guardar(@RequestBody MateriasPrimasRequest mp) {
        return materiasPrimasService.guardar(mp);
    }

    // Obtener todas
    @GetMapping
    public CodigoResponse<List<MateriasPrimasResponse>> obtenerTodas() {
        return materiasPrimasService.obtenerTodas();
    }

    // Obtener todas con proveedor
    @GetMapping("/con-proveedor")
    public CodigoResponse<List<MateriasPrimasConProveedorDTO>> obtenerTodasConProveedor() {
        return materiasPrimasService.obtenerTodasConProveedor();
    }

    // Obtener por ID
    @GetMapping("/{id}")
    public CodigoResponse<MateriasPrimasResponse> obtenerPorId(@PathVariable Long id) {
        return materiasPrimasService.obtenerPorId(id);
    }

    // Obtener por ID con proveedor
    @GetMapping("/con-proveedor/{id}")
    public CodigoResponse<MateriasPrimasConProveedorDTO> obtenerPorIdConProveedor(@PathVariable Long id) {
        return materiasPrimasService.obtenerPorIdConProveedor(id);
    }

    // Obtener por almacén
    @GetMapping("/almacen/{idAlmacen}")
    public CodigoResponse<List<MateriasPrimasResponse>> obtenerPorAlmacen(@PathVariable Long idAlmacen) {
        Almacen almacen = new Almacen();
        almacen.setId(idAlmacen);
        return materiasPrimasService.obtenerPorAlmacen(almacen);
    }

    // Obtener por proveedor
    @GetMapping("/proveedor/{idProveedor}")
    public CodigoResponse<List<MateriasPrimasResponse>> obtenerPorProveedor(@PathVariable Integer idProveedor) {
        return materiasPrimasService.obtenerPorProveedor(idProveedor);
    }

    // Eliminar por ID
    @DeleteMapping("/{id}")
    public CodigoResponse<Void> eliminarPorId(@PathVariable Long id) {
        return materiasPrimasService.eliminarPorId(id);
    }
}
