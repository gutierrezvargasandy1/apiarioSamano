package com.ApiarioSamano.MicroServiceApiarios.controller;

import com.ApiarioSamano.MicroServiceApiarios.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceApiarios.dto.ApiariosDTO.ApiarioRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.dto.RecetaDTO.RecetaRequest;
import com.ApiarioSamano.MicroServiceApiarios.model.Receta;
import com.ApiarioSamano.MicroServiceApiarios.model.Apiarios;
import com.ApiarioSamano.MicroServiceApiarios.model.Dispositivo;
import com.ApiarioSamano.MicroServiceApiarios.service.ApiariosService;
import com.ApiarioSamano.MicroServiceApiarios.service.SmartBee.MqttService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apiarios")
@RequiredArgsConstructor
public class ApiariosController {

    private final ApiariosService apiariosService;
    private final MqttService mqtt;

    // 🟢 Crear nuevo apiario
    @PostMapping
    public CodigoResponse<Apiarios> crearApiario(@RequestBody ApiarioRequestDTO apiarioDTO) {
        return apiariosService.crearApiario(apiarioDTO);
    }

    // 🟡 Modificar un apiario existente
    @PutMapping("/{id}")
    public CodigoResponse<Apiarios> modificarApiario(
            @PathVariable Long id,
            @RequestBody ApiarioRequestDTO datosActualizados) {
        return apiariosService.modificarApiario(id, datosActualizados);
    }

    // 🔴 Eliminar apiario
    @DeleteMapping("/{id}")
    public CodigoResponse<Void> eliminarApiario(@PathVariable Long id) {
        return apiariosService.eliminarApiario(id);
    }

    // 🔹 Agregar receta a un apiario
    @PostMapping("/{idApiario}/recetas")
    public CodigoResponse<Receta> agregarReceta(
            @PathVariable Long idApiario,
            @RequestBody RecetaRequest recetaDTO) {
        return apiariosService.agregarReceta(idApiario, recetaDTO);
    }

    // 🔹 Eliminar receta cumplida
    @DeleteMapping("/{idApiario}/receta")
    public ResponseEntity<CodigoResponse> eliminarRecetaCumplida(@PathVariable Long idApiario) {
        try {
            CodigoResponse response = apiariosService.eliminarRecetaCumplida(idApiario);
            return ResponseEntity.status(response.getCodigo()).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new CodigoResponse(500, "Error interno del servidor", null));
        }
    }

    // 🔍 Obtener todos los apiarios
    @GetMapping
    public CodigoResponse<List<Apiarios>> obtenerTodos() {
        return apiariosService.obtenerTodos();
    }

    // 🔍 Historial completo
    @GetMapping("/{idApiario}/historial-completo")
    public ResponseEntity<CodigoResponse> obtenerHistorialCompleto(@PathVariable Long idApiario) {
        try {
            CodigoResponse response = apiariosService.obtenerHistorialCompletoApiario(idApiario);
            return ResponseEntity.status(response.getCodigo()).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new CodigoResponse(500, "Error interno del servidor", null));
        }
    }

    // 🔍 Obtener apiario por ID
    @GetMapping("/{id}")
    public CodigoResponse<Apiarios> obtenerPorId(@PathVariable Long id) {
        return apiariosService.obtenerPorId(id);
    }

    // 🔍 Historial médico completo
    @GetMapping("/historial-medico/{idHistorial}")
    public CodigoResponse obtenerHistorialMedicoPorId(@PathVariable Long idHistorial) {
        return apiariosService.obtenerHistorialMedicoPorId(idHistorial);
    }

    // ===========================
    // ESTADO MQTT
    // ===========================
    @GetMapping("/mqtt/status")
    public String estadoMqtt() {
        return mqtt.estaConectado()
                ? "🟢 Conectado al broker MQTT"
                : "🔴 No conectado al broker MQTT";
    }

    // ===========================
    // COMANDOS MQTT
    // ===========================

    // 🌀 Ventilador
    @PostMapping("/{id}/ventilador/{estado}")
    public String ventilador(@PathVariable String id, @PathVariable boolean estado) {
        mqtt.enviarComandoVentilador(id, estado);
        return "Ventilador " + (estado ? "ENCENDIDO" : "APAGADO");
    }

    // 💡 Luz
    @PostMapping("/{id}/luz/{estado}")
    public String luz(@PathVariable String id, @PathVariable boolean estado) {
        mqtt.enviarComandoLuz(id, estado);
        return "Luz " + (estado ? "ENCENDIDA" : "APAGADA");
    }

    // 🔧 Servo 1
    @PostMapping("/{id}/servo1/{grados}")
    public String servo1(@PathVariable String id, @PathVariable int grados) {
        mqtt.enviarServo1(id, grados);
        return "Servo 1 movido a " + grados + " grados";
    }

    // 🔧 Servo 2
    @PostMapping("/{id}/servo2/{grados}")
    public String servo2(@PathVariable String id, @PathVariable int grados) {
        mqtt.enviarServo2(id, grados);
        return "Servo 2 movido a " + grados + " grados";
    }

    // ⚙️ Motor DC - L298N
    @PostMapping("/{id}/motor/{estado}")
    public String motorDC(@PathVariable String id, @PathVariable boolean estado) {
        mqtt.enviarMotorDC(id, estado);
        return "Motor DC " + (estado ? "ENCENDIDO" : "APAGADO");
    }

    // 🌈 LED RGB
    @PostMapping("/{id}/rgb/{r}/{g}/{b}")
    public String rgb(
            @PathVariable String id,
            @PathVariable int r,
            @PathVariable int g,
            @PathVariable int b) {

        mqtt.enviarRGB(id, r, g, b);
        return "LED RGB cambiado a (" + r + "," + g + "," + b + ")";
    }

    // 📡 Obtener dispositivos detectados
    @GetMapping("/dispositivos/detectados")
    public ResponseEntity<Map<String, Dispositivo>> obtenerDispositivosDetectados() {
        return ResponseEntity.ok(mqtt.getDispositivosDetectados());
    }

    // 📡 Obtener un dispositivo específico
    @GetMapping("/dispositivos/{dispositivoId}")
    public ResponseEntity<Dispositivo> obtenerDispositivo(@PathVariable String dispositivoId) {
        Dispositivo dispositivo = mqtt.getDispositivo(dispositivoId);
        if (dispositivo != null) {
            return ResponseEntity.ok(dispositivo);
        }
        return ResponseEntity.notFound().build();
    }
}
