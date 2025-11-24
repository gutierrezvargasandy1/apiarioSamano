package com.ApiarioSamano.MicroServiceGeneradorCodigo.controller;

import org.springframework.web.bind.annotation.*;
import com.ApiarioSamano.MicroServiceGeneradorCodigo.dto.*;
import com.ApiarioSamano.MicroServiceGeneradorCodigo.services.CodigoService;

@RestController
@RequestMapping("/api/codigos")
public class CodigoController implements ICodigoController {

    private final CodigoService codigoService;

    public CodigoController(CodigoService codigoService) {
        this.codigoService = codigoService;
    }

    @Override
    public CodigoResponseDTO generarContrasena() {
        String nuevaContrasena = codigoService.generarContrasena();
        return new CodigoResponseDTO(nuevaContrasena, "OK", "Contraseña generada correctamente.");
    }

    @Override
    public CodigoResponseDTO generarOTP() {
        String codigo = codigoService.generarOTP();
        return new CodigoResponseDTO(codigo, "OK", "Código OTP generado correctamente.");
    }

    @Override
    public CodigoResponseDTO generarLote(LoteRequest request) {
        String codigo = codigoService.generarCodigoLote(request.getProducto(), request.getNumeroLote());
        return new CodigoResponseDTO(
                codigo,
                "OK",
                "Código de lote generado para el apiario " + request.getProducto() + ".");
    }

    @Override
    public CodigoResponseDTO generarAlmacen(AlmacenRequest request) {
        String codigo = codigoService.generarCodigoAlmacen(request.getZona(), request.getProducto());
        return new CodigoResponseDTO(
                codigo,
                "OK",
                "Código de almacén generado para el producto " + request.getProducto() + ".");
    }

    @Override
    public CodigoResponseDTO generarIdArchivo() {
        String idArchivo = codigoService.generadorDeIdArchivos();
        return new CodigoResponseDTO(
                idArchivo,
                "OK",
                "ID de archivo generado correctamente.");
    }
}
