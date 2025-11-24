package com.ApiarioSamano.MicroServiceGeneradorCodigo.services;

import org.springframework.stereotype.Service;

import com.ApiarioSamano.MicroServiceGeneradorCodigo.exceptions.CodigoException;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class CodigoService {

    private final Random random = new Random();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
    private SecureRandom secureRandom = new SecureRandom();

    public String generarOTP() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public String generarContrasena() {
        int longitud = 12;
        int longitudMinima = 10;
        if (longitud < longitudMinima) {
            throw new CodigoException("La contraseña debe tener al menos " + longitudMinima + " caracteres.");
        }
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public String generarCodigoLote(String apiario, int numeroLote) {
        if (apiario == null || apiario.isEmpty()) {
            throw new CodigoException("El parámetro 'apiario' no puede estar vacío.");
        }
        if (numeroLote <= 0) {
            throw new CodigoException("El número de lote debe ser mayor que cero.");
        }

        LocalDate fecha = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyMMdd");
        String fechaStr = fecha.format(formato);
        return apiario.toUpperCase() + fechaStr + "L" + String.format("%02d", numeroLote);
    }

    public String generarCodigoAlmacen(String zona, String producto) {
        if (zona == null || zona.isEmpty()) {
            throw new CodigoException("El parámetro 'zona' no puede estar vacío.");
        }
        if (producto == null || producto.isEmpty()) {
            throw new CodigoException("El parámetro 'producto' no puede estar vacío.");
        }

        LocalDate fecha = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyMMdd");
        String fechaStr = fecha.format(formato);
        int aleatorio = 1000 + random.nextInt(9000);
        return zona.toUpperCase() + "-" + producto.toUpperCase() + "-" + fechaStr + "-" + aleatorio;
    }

    public String generadorDeIdArchivos() {
        LocalDateTime fecha = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String fechaStr = fecha.format(formato);

        // Generar una cadena aleatoria corta
        StringBuilder aleatorio = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            aleatorio.append(CHARACTERS.charAt(secureRandom.nextInt(CHARACTERS.length())));
        }

        return "FILE-" + fechaStr + "-" + aleatorio;
    }
}
