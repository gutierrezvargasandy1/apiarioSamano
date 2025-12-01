package com.ApiarioSamano.MicroServiceApiarios.service.SmartBee;

import com.ApiarioSamano.MicroServiceApiarios.config.SensorWebSocketHandler;
import com.ApiarioSamano.MicroServiceApiarios.dto.DispositivoDTO.DispositivoRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.factory.DispositivoFactory;
import com.ApiarioSamano.MicroServiceApiarios.model.Dispositivo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MqttService {

    @Value("${mqtt.host}")
    private String mqttHost;

    @Value("${mqtt.port}")
    private int mqttPort;

    @Value("${mqtt.client}")
    private String clientId;

    private MqttClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final DispositivoFactory dispositivoFactory;

    // Almacenar dispositivos detectados
    private final Map<String, Dispositivo> dispositivosDetectados = new ConcurrentHashMap<>();

    // Últimos datos de sensores
    private final Map<String, Map<String, String>> ultimosDatosSensores = new ConcurrentHashMap<>();

    // Timestamps de sensores
    private final Map<String, Map<String, Long>> timestampsSensores = new ConcurrentHashMap<>();

    // ========================
    // CONEXIÓN
    // ========================
    @PostConstruct
    public void connect() {
        try {
            String url = "tcp://" + mqttHost + ":" + mqttPort;
            client = new MqttClient(url, clientId, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            client.connect(options);

            System.out.println("📡 Conectado al broker MQTT: " + url);

            // Suscripción
            client.subscribe("apiarios/dispositivos/#", (topic, msg) -> {
                String payload = new String(msg.getPayload());
                procesarMensaje(topic, payload);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================
    // 🔍 PROCESAR MENSAJES RECIBIDOS
    // ================================
    private void procesarMensaje(String topic, String payload) {
        System.out.println("📥 Mensaje recibido -> Topic: " + topic + "   Payload: " + payload);

        if (topic.equals("apiarios/dispositivos/registro")) {
            procesarRegistroDispositivo(payload);
        } else if (topic.contains("/dispositivos/") && topic.contains("/status")) {
            procesarEstado(topic, payload);
        } else if (topic.contains("/dispositivos/") && topic.contains("/humedad_suelo")) {
            procesarDatoSensor(topic, payload, "humedad_suelo");
        } else if (topic.contains("/dispositivos/") && topic.contains("/sensor_status")) {
            procesarEstadoSensor(topic, payload);
        }
    }

    // ================================
    // 📡 PROCESAR REGISTRO DE DISPOSITIVO (FACTORY METHOD)
    // ================================
    private void procesarRegistroDispositivo(String json) {
        try {
            // Convertimos JSON a DTO
            DispositivoRequestDTO dto = objectMapper.readValue(json, DispositivoRequestDTO.class);

            // Creamos el dispositivo mediante la fábrica
            Dispositivo dispositivo = dispositivoFactory.crear(dto);

            // Guardamos en memoria
            dispositivosDetectados.put(dispositivo.getDispositivoId(), dispositivo);

            System.out.println("✅ Dispositivo registrado (Factory Method):");
            System.out.println("   - ID Dispositivo: " + dispositivo.getDispositivoId());
            System.out.println("   - Tipo: " + dispositivo.getTipo());
            System.out.println("   - Sensores: " + dispositivo.getSensores());
            System.out.println("   - Actuadores: " + dispositivo.getActuadores());

        } catch (Exception e) {
            System.err.println("❌ Error procesando registro de dispositivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================================
    // 📊 PROCESAR DATOS SENSOR
    // ================================
    private void procesarDatoSensor(String topic, String payload, String tipoSensor) {
        String dispositivoId = extraerDispositivoId(topic);

        if (payload.equals("SENSOR_DESCONECTADO") || payload.contains("DESCONECTADO")) {
            if (ultimosDatosSensores.containsKey(dispositivoId)) {
                ultimosDatosSensores.get(dispositivoId).remove(tipoSensor);
            }

            enviarPorWebSocket(dispositivoId, tipoSensor, "SENSOR_DESCONECTADO");

            System.out.println("❌ Sensor " + tipoSensor + " de " + dispositivoId + ": DESCONECTADO");
            return;
        }

        ultimosDatosSensores
                .computeIfAbsent(dispositivoId, k -> new ConcurrentHashMap<>())
                .put(tipoSensor, payload);

        timestampsSensores
                .computeIfAbsent(dispositivoId, k -> new ConcurrentHashMap<>())
                .put(tipoSensor, System.currentTimeMillis());

        enviarPorWebSocket(dispositivoId, tipoSensor, payload);

        System.out.println("📊 " + tipoSensor + " de " + dispositivoId + ": " + payload);
    }

    private void enviarPorWebSocket(String dispositivoId, String tipoSensor, String valor) {
        try {
            String mensajeWebSocket = String.format("""
                    {
                        "dispositivoId": "%s",
                        "sensor": "%s",
                        "valor": "%s",
                        "timestamp": "%d"
                    }
                    """, dispositivoId, tipoSensor, valor, System.currentTimeMillis());

            SensorWebSocketHandler.enviarATodos(mensajeWebSocket);

        } catch (Exception e) {
            System.err.println("❌ Error enviando por WebSocket: " + e.getMessage());
        }
    }

    private void procesarEstado(String topic, String payload) {
        String dispositivoId = extraerDispositivoId(topic);
        System.out.println("🟢 Estado de " + dispositivoId + ": " + payload);
    }

    private void procesarEstadoSensor(String topic, String payload) {
        String dispositivoId = extraerDispositivoId(topic);
        System.out.println("🔍 Estado sensor de " + dispositivoId + ": " + payload);
    }

    private String extraerDispositivoId(String topic) {
        String[] partes = topic.split("/");
        for (int i = 0; i < partes.length; i++) {
            if (partes[i].equals("dispositivos") && i + 1 < partes.length) {
                return partes[i + 1];
            }
        }
        return "desconocido";
    }

    // ================================
    // 🔄 LIMPIAR DATOS ANTIGUOS
    // ================================
    @Scheduled(fixedRate = 30000)
    public void limpiarDatosAntiguos() {
        long ahora = System.currentTimeMillis();
        long UMBRAL_DESCONEXION = 45000;

        System.out.println("🔄 Revisando datos de sensores antiguos...");

        for (String dispositivoId : timestampsSensores.keySet()) {
            Map<String, Long> timestamps = timestampsSensores.get(dispositivoId);

            for (String sensor : timestamps.keySet()) {
                long ultimaActualizacion = timestamps.get(sensor);
                long tiempoInactivo = ahora - ultimaActualizacion;

                if (tiempoInactivo > UMBRAL_DESCONEXION) {
                    System.out.println("❌ Sensor " + sensor + " de " + dispositivoId + " inactivo por "
                            + (tiempoInactivo / 1000) + " segundos");

                    if (ultimosDatosSensores.containsKey(dispositivoId)) {
                        ultimosDatosSensores.get(dispositivoId).remove(sensor);
                    }

                    enviarPorWebSocket(dispositivoId, sensor, "SENSOR_DESCONECTADO");
                }
            }
        }
    }

    // ================================
    // 🧹 LIMPIEZA MANUAL
    // ================================
    public void limpiarDatosDispositivo(String dispositivoId) {
        if (ultimosDatosSensores.containsKey(dispositivoId)) {
            ultimosDatosSensores.get(dispositivoId).clear();
        }
        if (timestampsSensores.containsKey(dispositivoId)) {
            timestampsSensores.get(dispositivoId).clear();
        }
        System.out.println("🧹 Datos limpiados para dispositivo: " + dispositivoId);
    }

    // ================================
    // GETTERS
    // ================================
    public Map<String, Dispositivo> getDispositivosDetectados() {
        return dispositivosDetectados;
    }

    public Dispositivo getDispositivo(String dispositivoId) {
        return dispositivosDetectados.get(dispositivoId);
    }

    public Map<String, String> getUltimosDatosSensores(String dispositivoId) {
        Map<String, String> datos = ultimosDatosSensores.getOrDefault(dispositivoId, new HashMap<>());

        Map<String, Long> timestamps = timestampsSensores.getOrDefault(dispositivoId, new HashMap<>());
        long ahora = System.currentTimeMillis();
        long UMBRAL_DESCONEXION = 45000;

        for (String sensor : datos.keySet()) {
            if (timestamps.containsKey(sensor)) {
                long tiempoInactivo = ahora - timestamps.get(sensor);
                if (tiempoInactivo > UMBRAL_DESCONEXION) {
                    datos.put(sensor, "SENSOR_DESCONECTADO");
                }
            }
        }

        return datos;
    }

    public boolean estaConectado() {
        return client != null && client.isConnected();
    }

    // ========================
    // PUBLICACIÓN DE COMANDOS
    // ========================
    public void enviarComandoVentilador(String dispositivoId, boolean estado) {
        publicar("apiarios/dispositivos/" + dispositivoId + "/comandos/ventilador", estado ? "ON" : "OFF");
    }

    public void enviarComandoCompuerta(String dispositivoId, boolean estado) {
        publicar("apiarios/dispositivos/" + dispositivoId + "/comandos/compuerta", estado ? "ON" : "OFF");
    }

    public void enviarComandoLuz(String dispositivoId, boolean estado) {
        publicar("apiarios/dispositivos/" + dispositivoId + "/comandos/luz", estado ? "ON" : "OFF");
    }

    public void enviarServo1(String dispositivoId, int grados) {
        publicar("apiarios/dispositivos/" + dispositivoId + "/comandos/servo1", String.valueOf(grados));
    }

    public void enviarServo2(String dispositivoId, int grados) {
        publicar("apiarios/dispositivos/" + dispositivoId + "/comandos/servo2", String.valueOf(grados));
    }

    private void publicar(String topic, String mensaje) {
        try {
            if (!estaConectado()) {
                System.out.println("⚠️ Cliente MQTT desconectado, reconectando...");
                connect();
            }

            MqttMessage msg = new MqttMessage(mensaje.getBytes());
            msg.setQos(1);

            client.publish(topic, msg);

            System.out.println("📤 Publicado -> Topic: " + topic + "   Mensaje: " + mensaje);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
