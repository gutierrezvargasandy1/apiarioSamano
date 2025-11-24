package com.example.MicroServiceGestorDeArchivos.controller;

import com.example.MicroServiceGestorDeArchivos.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ver")
@RequiredArgsConstructor
class VistasController {

    private final FileStorageService fileStorageService;

    @GetMapping("/view/{id}")
    public ResponseEntity<Resource> viewFile(@PathVariable String id) {
        return fileStorageService.verArchivo(id);
    }
}
