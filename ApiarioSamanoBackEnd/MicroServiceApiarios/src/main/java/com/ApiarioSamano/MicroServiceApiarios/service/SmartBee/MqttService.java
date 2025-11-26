package com.ApiarioSamano.MicroServiceApiarios.service.SmartBee;

import com.ApiarioSamano.MicroServiceApiarios.model.Dispositivo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    // Almacenar dispositivos detectados (en memoria, puedes cambiarlo a BD)
    private final Map<String, Dispositivo> dispositivosDetectados = new ConcurrentHashMap<>();

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

            // Suscribirse a todos los topics de apiarios
            client.subscribe("apiarios/#", (topic, msg) -> {
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

        // Detectar si es un registro de dispositivo
        if (topic.equals("apiarios/dispositivos/registro")) {
            procesarRegistroDispositivo(payload);
        }
        // Puedes agregar más handlers aquí para otros topics
        else if (topic.contains("/temperatura")) {
            procesarTemperatura(topic, payload);
        } else if (topic.contains("/humedad")) {
            procesarHumedad(topic, payload);
        } else if (topic.contains("/status")) {
            procesarEstado(topic, payload);
        }
    }

    // ================================
    // 📡 PROCESAR REGISTRO DE DISPOSITIVO
    // ================================
    private void procesarRegistroDispositivo(String json) {
        try {
            // Parsear el JSON del dispositivo
            Dispositivo dispositivo = objectMapper.readValue(json, Dispositivo.class);

            // Guardar en memoria
            dispositivosDetectados.put(dispositivo.getDispositivoId(), dispositivo);

            System.out.println("✅ Dispositivo registrado:");
            System.out.println("   - ID Dispositivo: " + dispositivo.getDispositivoId());
            System.out.println("   - Apiario ID: " + dispositivo.getApiarioId());
            System.out.println("   - Tipo: " + dispositivo.getTipo());
            System.out.println("   - Sensores: " + dispositivo.getSensores());
            System.out.println("   - Actuadores: " + dispositivo.getActuadores());

            // Aquí puedes agregar lógica para guardar en BD
            // Por ejemplo: apiariosService.vincularDispositivo(dispositivo);

        } catch (Exception e) {
            System.err.println("❌ Error procesando registro de dispositivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================================
    // 📊 PROCESAR DATOS DE SENSORES
    // ================================
    private void procesarTemperatura(String topic, String payload) {
        // Extraer el apiarioId del topic: apiarios/apiario_001/temperatura
        String apiarioId = extraerApiarioId(topic);
        System.out.println("🌡️ Temperatura de " + apiarioId + ": " + payload + "°C");
        // Aquí puedes guardar en BD o emitir evento
    }

    private void procesarHumedad(String topic, String payload) {
        String apiarioId = extraerApiarioId(topic);
        System.out.println("💧 Humedad de " + apiarioId + ": " + payload + "%");
        // Aquí puedes guardar en BD o emitir evento
    }

    private void procesarEstado(String topic, String payload) {
        String apiarioId = extraerApiarioId(topic);
        System.out.println("🟢 Estado de " + apiarioId + ": " + payload);
        // Aquí puedes actualizar estado en BD
    }

    private String extraerApiarioId(String topic) {
        // De "apiarios/apiario_001/temperatura" extraer "apiario_001"
        String[] partes = topic.split("/");
        return partes.length > 1 ? partes[1] : "desconocido";
    }

    // ================================
    // 📋 OBTENER DISPOSITIVOS DETECTADOS
    // ================================
    public Map<String, Dispositivo> getDispositivosDetectados() {
        return dispositivosDetectados;
    }

    public Dispositivo getDispositivo(String dispositivoId) {
        return dispositivosDetectados.get(dispositivoId);
    }

    // ================================
    // 🔍 MÉTODO PARA VALIDAR CONEXIÓN
    // ================================
    public boolean estaConectado() {
        return client != null && client.isConnected();
    }

    // ========================
    // PUBLICAR COMANDOS
    // ========================

    public void enviarComandoVentilador(String apiarioId, boolean estado) {
        publicar("apiarios/" + apiarioId + "/comandos/ventilador", estado ? "ON" : "OFF");
    }

    public void enviarComandoLuz(String apiarioId, boolean estado) {
        publicar("apiarios/" + apiarioId + "/comandos/luz", estado ? "ON" : "OFF");
    }

    public void enviarComandoServo(String apiarioId, int grados) {
        publicar("apiarios/" + apiarioId + "/comandos/servo", String.valueOf(grados));
    }

    // ========================
    // 🔧 NUEVOS COMANDOS
    // ========================

    public void enviarServo1(String apiarioId, int grados) {
        publicar("apiarios/" + apiarioId + "/comandos/servo1", String.valueOf(grados));
    }

    public void enviarServo2(String apiarioId, int grados) {
        publicar("apiarios/" + apiarioId + "/comandos/servo2", String.valueOf(grados));
    }

    public void enviarMotorDC(String apiarioId, boolean estado) {
        publicar("apiarios/" + apiarioId + "/comandos/motor", estado ? "ON" : "OFF");
    }

    public void enviarRGB(String apiarioId, int r, int g, int b) {
        publicar("apiarios/" + apiarioId + "/comandos/rgb", r + "," + g + "," + b);
    }

    // ========================
    // MÉTODO GENERAL DE ENVÍO
    // ========================
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
