package com.ApiarioSamano.MicroServiceGeneradorCodigo.controller;

import com.ApiarioSamano.MicroServiceGeneradorCodigo.dto.*;
import org.springframework.web.bind.annotation.*;

public interface ICodigoController {

        /**
         * Genera un código OTP (One Time Password).
         * No requiere parámetros JSON.
         * GET /api/codigos/otp
         */
        @GetMapping("/otp")
        CodigoResponseDTO generarOTP();

        /**
         * Genera un código de lote para un apiario específico.
         * Recibe un JSON con los siguientes campos:
         * {
         * "apiario": "A", // Nombre o identificador del apiario
         * "numeroLote": 1 // Número del lote a generar
         * }
         * POST /api/codigos/lote
         */
        @PostMapping("/lote")
        CodigoResponseDTO generarLote(@RequestBody LoteRequest request);

        /**
         * Genera un código de almacén para un producto en una zona específica.
         * Recibe un JSON con los siguientes campos:
         * {
         * "zona": "Z1", // Identificador de la zona de almacenamiento
         * "producto": "PRD" // Código o nombre del producto
         * }
         * POST /api/codigos/almacen
         */
        @PostMapping("/almacen")
        CodigoResponseDTO generarAlmacen(@RequestBody AlmacenRequest request);

        /**
         * Genera una contraseña segura aleatoria.
         * GET /api/contrasena/generar
         * 
         * @return ResponseDTO<String> con la nueva contraseña generada
         */
        @GetMapping("/contrasena")
        CodigoResponseDTO generarContrasena();

        /**
         * Genera un identificador único para archivos.
         * GET /api/codigos/archivo
         */
        @GetMapping("/archivo")
        CodigoResponseDTO generarIdArchivo();
}
