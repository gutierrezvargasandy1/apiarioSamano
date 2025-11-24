package com.ApiarioSamano.MicroServiceProduccion.controller;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.ProductoDTO.ProductoRequest;
import com.ApiarioSamano.MicroServiceProduccion.dto.ProductoDTO.ProductoResponse;
import com.ApiarioSamano.MicroServiceProduccion.model.Producto;
import com.ApiarioSamano.MicroServiceProduccion.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping("/crear")
    public CodigoResponse<Producto> crearProducto(@RequestBody ProductoRequest request) {
        return productoService.crearProducto(request);
    }

    @PutMapping("/{id}")
    public CodigoResponse<Producto> actualizarProducto(
            @PathVariable Long id,
            @RequestBody ProductoRequest request) {
        return productoService.actualizarProducto(id, request);
    }

    @GetMapping("/listar")
    public CodigoResponse<List<ProductoResponse>> listarProductosActivos() {
        return productoService.listarProductosActivos();
    }

    @GetMapping("/{id}")
    public CodigoResponse<ProductoResponse> obtenerProductoPorId(@PathVariable Long id) {
        return productoService.obtenerProductoPorId(id);
    }

    @GetMapping("/lote/{idLote}")
    public CodigoResponse<List<ProductoResponse>> obtenerProductosPorLote(@PathVariable Long idLote) {
        return productoService.obtenerProductosPorLote(idLote);
    }

    @PutMapping("/{id}/desactivar")
    public CodigoResponse<Void> desactivarProducto(@PathVariable Long id) {
        return productoService.desactivarProducto(id);
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> obtenerFotoProducto(@PathVariable Long id) {
        byte[] foto = productoService.obtenerFotoProducto(id);

        if (foto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"producto-" + id + ".jpg\"")
                .body(foto);
    }

    @PutMapping("/{id}/foto")
    public CodigoResponse<Void> actualizarFotoProducto(
            @PathVariable Long id,
            @RequestParam("foto") MultipartFile file) {
        try {
            byte[] fotoBytes = file.getBytes();
            return productoService.actualizarFotoProducto(id, fotoBytes);
        } catch (IOException e) {
            return new CodigoResponse<>(500, "Error al procesar la imagen", null);
        }
    }
}