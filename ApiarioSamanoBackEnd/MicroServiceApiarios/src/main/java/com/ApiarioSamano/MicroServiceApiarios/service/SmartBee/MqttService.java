package com.ApiarioSamano.MicroServiceApiarios.service.SmartBee;

import com.ApiarioSamano.MicroServiceApiarios.config.SensorWebSocketHandler;
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

    // Almacenar dispositivos detectados (en memoria, puedes cambiarlo a BD)
    private final Map<String, Dispositivo> dispositivosDetectados = new ConcurrentHashMap<>();

    // 📊 Almacenar ÚLTIMOS datos de sensores en memoria (solo para consulta
    // directa)
    private final Map<String, Map<String, String>> ultimosDatosSensores = new ConcurrentHashMap<>();

    // ⏰ Almacenar timestamps de última actualización
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
        // Procesar datos de sensores y estado
        else if (topic.contains("/temperatura")) {
            procesarDatoSensor(topic, payload, "temperatura");
        } else if (topic.contains("/humedad_ambiente")) {
            procesarDatoSensor(topic, payload, "humedad_ambiente");
        } else if (topic.contains("/humedad_suelo")) {
            procesarDatoSensor(topic, payload, "humedad_suelo");
        } else if (topic.contains("/peso")) {
            procesarDatoSensor(topic, payload, "peso");
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
    // 📊 PROCESAR DATOS DE SENSORES EN TIEMPO REAL
    // ================================
    private void procesarDatoSensor(String topic, String payload, String tipoSensor) {
        String apiarioId = extraerApiarioId(topic);

        // 🔥 DETECTAR SI EL SENSOR ESTÁ DESCONECTADO
        if (payload.equals("SENSOR_DESCONECTADO") || payload.contains("DESCONECTADO")) {
            // Limpiar datos cuando el sensor se desconecta
            if (ultimosDatosSensores.containsKey(apiarioId)) {
                ultimosDatosSensores.get(apiarioId).remove(tipoSensor);
            }

            // 🔥 ENVIAR MENSAJE DE DESCONEXIÓN POR WEBSOCKET
            enviarPorWebSocket(apiarioId, tipoSensor, "SENSOR_DESCONECTADO");

            System.out.println("❌ Sensor " + tipoSensor + " de " + apiarioId + ": DESCONECTADO");
            return;
        }

        // 🔥 GUARDAR SOLO EL ÚLTIMO DATO (no historial)
        ultimosDatosSensores
                .computeIfAbsent(apiarioId, k -> new ConcurrentHashMap<>())
                .put(tipoSensor, payload);

        // 🔥 GUARDAR TIMESTAMP DE ACTUALIZACIÓN
        timestampsSensores
                .computeIfAbsent(apiarioId, k -> new ConcurrentHashMap<>())
                .put(tipoSensor, System.currentTimeMillis());

        // 🔥 ENVIAR A WEBSOCKET PARA TIEMPO REAL
        enviarPorWebSocket(apiarioId, tipoSensor, payload);

        System.out.println("📊 " + tipoSensor + " de " + apiarioId + ": " + payload);
    }

    // ================================
    // 🔥 ENVIAR DATOS POR WEBSOCKET
    // ================================
    private void enviarPorWebSocket(String apiarioId, String tipoSensor, String valor) {
        try {
            // Crear mensaje JSON para WebSocket
            String mensajeWebSocket = String.format("""
                    {
                        "apiarioId": "%s",
                        "sensor": "%s",
                        "valor": "%s",
                        "timestamp": "%d"
                    }
                    """, apiarioId, tipoSensor, valor, System.currentTimeMillis());

            // 🔥 ENVIAR A TODOS LOS CLIENTES CONECTADOS
            SensorWebSocketHandler.enviarATodos(mensajeWebSocket);

        } catch (Exception e) {
            System.err.println("❌ Error enviando por WebSocket: " + e.getMessage());
        }
    }

    private void procesarEstado(String topic, String payload) {
        String apiarioId = extraerApiarioId(topic);
        System.out.println("🟢 Estado de " + apiarioId + ": " + payload);
        // Aquí puedes actualizar estado en BD
    }

    private String extraerApiarioId(String topic) {
        // De "apiarios/apiario_001/status" extraer "apiario_001"
        String[] partes = topic.split("/");
        return partes.length > 1 ? partes[1] : "desconocido";
    }

    // ================================
    // 🔄 LIMPIAR DATOS ANTIGUOS (CADA 30 SEGUNDOS)
    // ================================
    @Scheduled(fixedRate = 30000)
    public void limpiarDatosAntiguos() {
        long ahora = System.currentTimeMillis();
        long UMBRAL_DESCONEXION = 45000; // 45 segundos sin datos = desconectado

        System.out.println("🔄 Revisando datos de sensores antiguos...");

        for (String apiarioId : timestampsSensores.keySet()) {
            Map<String, Long> timestamps = timestampsSensores.get(apiarioId);

            for (String sensor : timestamps.keySet()) {
                long ultimaActualizacion = timestamps.get(sensor);
                long tiempoInactivo = ahora - ultimaActualizacion;

                if (tiempoInactivo > UMBRAL_DESCONEXION) {
                    // 🔥 MARCAR SENSOR COMO DESCONECTADO
                    System.out.println("❌ Sensor " + sensor + " de " + apiarioId + " inactivo por "
                            + (tiempoInactivo / 1000) + " segundos");

                    // Limpiar dato
                    if (ultimosDatosSensores.containsKey(apiarioId)) {
                        ultimosDatosSensores.get(apiarioId).remove(sensor);
                    }

                    // Enviar notificación por WebSocket
                    enviarPorWebSocket(apiarioId, sensor, "SENSOR_DESCONECTADO");
                }
            }
        }
    }

    // ================================
    // 🧹 MÉTODO PARA LIMPIAR DATOS MANUALMENTE
    // ================================
    public void limpiarDatosApiario(String apiarioId) {
        if (ultimosDatosSensores.containsKey(apiarioId)) {
            ultimosDatosSensores.get(apiarioId).clear();
        }
        if (timestampsSensores.containsKey(apiarioId)) {
            timestampsSensores.get(apiarioId).clear();
        }
        System.out.println("🧹 Datos limpiados para apiario: " + apiarioId);
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
    // 📊 OBTENER ÚLTIMOS DATOS DE SENSORES (para polling)
    // ================================
    public Map<String, String> getUltimosDatosSensores(String apiarioId) {
        Map<String, String> datos = ultimosDatosSensores.getOrDefault(apiarioId, new HashMap<>());

        // 🔥 VERIFICAR SI LOS DATOS SON RECIENTES
        Map<String, Long> timestamps = timestampsSensores.getOrDefault(apiarioId, new HashMap<>());
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

    // ================================
    // 🔍 MÉTODO PARA VALIDAR CONEXIÓN
    // ================================
    public boolean estaConectado() {
        return client != null && client.isConnected();
    }

    // ========================
    // PUBLICAR COMANDOS - ACTUALIZADOS
    // ========================

    // 🌀 Ventilador (Motor A)
    public void enviarComandoVentilador(String apiarioId, boolean estado) {
        publicar("apiarios/" + apiarioId + "/comandos/ventilador", estado ? "ON" : "OFF");
    }

    // 🚪 Compuerta (Motor B)
    public void enviarComandoCompuerta(String apiarioId, boolean estado) {
        publicar("apiarios/" + apiarioId + "/comandos/compuerta", estado ? "ON" : "OFF");
    }

    // 💡 Luz
    public void enviarComandoLuz(String apiarioId, boolean estado) {
        publicar("apiarios/" + apiarioId + "/comandos/luz", estado ? "ON" : "OFF");
    }

    // 🔧 Servo 1
    public void enviarServo1(String apiarioId, int grados) {
        publicar("apiarios/" + apiarioId + "/comandos/servo1", String.valueOf(grados));
    }

    // 🔧 Servo 2
    public void enviarServo2(String apiarioId, int grados) {
        publicar("apiarios/" + apiarioId + "/comandos/servo2", String.valueOf(grados));
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