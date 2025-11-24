package com.example.MicroServiceGestorDeArchivos.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.MicroServiceGestorDeArchivos.dto.FileUploadResponse;
import com.example.MicroServiceGestorDeArchivos.service.FileStorageService;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    public FileStorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 📤 Subir un archivo
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            FileUploadResponse response = fileStorageService.guardarArchivo(file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📥 Descargar un archivo
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        return fileStorageService.descargarArchivo(id);
    }

    /**
     * 🗑️ Eliminar un archivo físico y su registro en la BD
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        try {
            fileStorageService.eliminarArchivo(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ♻️ Actualizar solo el archivo físico sin tocar la BD
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateFilePhysical(@PathVariable String id,
            @RequestParam("file") MultipartFile newFile) {
        try {
            fileStorageService.actualizarArchivoFisico(id, newFile);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
