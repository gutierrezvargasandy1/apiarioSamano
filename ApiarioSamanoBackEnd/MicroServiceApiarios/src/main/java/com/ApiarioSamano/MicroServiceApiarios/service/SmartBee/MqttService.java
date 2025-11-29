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

            // Suscribirse a todos los topics de dispositivos
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

        // Detectar si es un registro de dispositivo
        if (topic.equals("apiarios/dispositivos/registro")) {
            procesarRegistroDispositivo(payload);
        }
        // Procesar datos de sensores y estado (usando dispositivoId)
        else if (topic.contains("/dispositivos/") && topic.contains("/status")) {
            procesarEstado(topic, payload);
        } else if (topic.contains("/dispositivos/") && topic.contains("/humedad_suelo")) {
            procesarDatoSensor(topic, payload, "humedad_suelo");
        } else if (topic.contains("/dispositivos/") && topic.contains("/sensor_status")) {
            procesarEstadoSensor(topic, payload);
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
            System.out.println("   - Tipo: " + dispositivo.getTipo());
            System.out.println("   - Sensores: " + dispositivo.getSensores());
            System.out.println("   - Actuadores: " + dispositivo.getActuadores());

        } catch (Exception e) {
            System.err.println("❌ Error procesando registro de dispositivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================================
    // 📊 PROCESAR DATOS DE SENSORES EN TIEMPO REAL
    // ================================
    private void procesarDatoSensor(String topic, String payload, String tipoSensor) {
        String dispositivoId = extraerDispositivoId(topic);

        // 🔥 DETECTAR SI EL SENSOR ESTÁ DESCONECTADO
        if (payload.equals("SENSOR_DESCONECTADO") || payload.contains("DESCONECTADO")) {
            // Limpiar datos cuando el sensor se desconecta
            if (ultimosDatosSensores.containsKey(dispositivoId)) {
                ultimosDatosSensores.get(dispositivoId).remove(tipoSensor);
            }

            // 🔥 ENVIAR MENSAJE DE DESCONEXIÓN POR WEBSOCKET
            enviarPorWebSocket(dispositivoId, tipoSensor, "SENSOR_DESCONECTADO");

            System.out.println("❌ Sensor " + tipoSensor + " de " + dispositivoId + ": DESCONECTADO");
            return;
        }

        // 🔥 GUARDAR SOLO EL ÚLTIMO DATO (no historial)
        ultimosDatosSensores
                .computeIfAbsent(dispositivoId, k -> new ConcurrentHashMap<>())
                .put(tipoSensor, payload);

        // 🔥 GUARDAR TIMESTAMP DE ACTUALIZACIÓN
        timestampsSensores
                .computeIfAbsent(dispositivoId, k -> new ConcurrentHashMap<>())
                .put(tipoSensor, System.currentTimeMillis());

        // 🔥 ENVIAR A WEBSOCKET PARA TIEMPO REAL
        enviarPorWebSocket(dispositivoId, tipoSensor, payload);

        System.out.println("📊 " + tipoSensor + " de " + dispositivoId + ": " + payload);
    }

    // ================================
    // 🔥 ENVIAR DATOS POR WEBSOCKET
    // ================================
    private void enviarPorWebSocket(String dispositivoId, String tipoSensor, String valor) {
        try {
            // Crear mensaje JSON para WebSocket
            String mensajeWebSocket = String.format("""
                    {
                        "dispositivoId": "%s",
                        "sensor": "%s",
                        "valor": "%s",
                        "timestamp": "%d"
                    }
                    """, dispositivoId, tipoSensor, valor, System.currentTimeMillis());

            // 🔥 ENVIAR A TODOS LOS CLIENTES CONECTADOS
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
        // De "apiarios/dispositivos/b0a73222f640/status" extraer "b0a73222f640"
        String[] partes = topic.split("/");
        for (int i = 0; i < partes.length; i++) {
            if (partes[i].equals("dispositivos") && i + 1 < partes.length) {
                return partes[i + 1];
            }
        }
        return "desconocido";
    }

    // ================================
    // 🔄 LIMPIAR DATOS ANTIGUOS (CADA 30 SEGUNDOS)
    // ================================
    @Scheduled(fixedRate = 30000)
    public void limpiarDatosAntiguos() {
        long ahora = System.currentTimeMillis();
        long UMBRAL_DESCONEXION = 45000; // 45 segundos sin datos = desconectado

        System.out.println("🔄 Revisando datos de sensores antiguos...");

        for (String dispositivoId : timestampsSensores.keySet()) {
            Map<String, Long> timestamps = timestampsSensores.get(dispositivoId);

            for (String sensor : timestamps.keySet()) {
                long ultimaActualizacion = timestamps.get(sensor);
                long tiempoInactivo = ahora - ultimaActualizacion;

                if (tiempoInactivo > UMBRAL_DESCONEXION) {
                    // 🔥 MARCAR SENSOR COMO DESCONECTADO
                    System.out.println("❌ Sensor " + sensor + " de " + dispositivoId + " inactivo por "
                            + (tiempoInactivo / 1000) + " segundos");

                    // Limpiar dato
                    if (ultimosDatosSensores.containsKey(dispositivoId)) {
                        ultimosDatosSensores.get(dispositivoId).remove(sensor);
                    }

                    // Enviar notificación por WebSocket
                    enviarPorWebSocket(dispositivoId, sensor, "SENSOR_DESCONECTADO");
                }
            }
        }
    }

    // ================================
    // 🧹 MÉTODO PARA LIMPIAR DATOS MANUALMENTE
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
    public Map<String, String> getUltimosDatosSensores(String dispositivoId) {
        Map<String, String> datos = ultimosDatosSensores.getOrDefault(dispositivoId, new HashMap<>());

        // 🔥 VERIFICAR SI LOS DATOS SON RECIENTES
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

    // ================================
    // 🔍 MÉTODO PARA VALIDAR CONEXIÓN
    // ================================
    public boolean estaConectado() {
        return client != null && client.isConnected();
    }

    // ========================
    // PUBLICAR COMANDOS - ACTUALIZADOS PARA DISPOSITIVOS
    // ========================

    // 🌀 Ventilador (Motor A)
    public void enviarComandoVentilador(String dispositivoId, boolean estado) {
        publicar("apiarios/dispositivos/" + dispositivoId + "/comandos/ventilador", estado ? "ON" : "OFF");
    }

    // 🚪 Compuerta (Motor B)
    public void enviarComandoCompuerta(String dispositivoId, boolean estado) {
        publicar("apiarios/dispositivos/" + dispositivoId + "/comandos/compuerta", estado ? "ON" : "OFF");
    }

    // 💡 Luz
    public void enviarComandoLuz(String dispositivoId, boolean estado) {
        publicar("apiarios/dispositivos/" + dispositivoId + "/comandos/luz", estado ? "ON" : "OFF");
    }

    // 🔧 Servo 1
    public void enviarServo1(String dispositivoId, int grados) {
        publicar("apiarios/dispositivos/" + dispositivoId + "/comandos/servo1", String.valueOf(grados));
    }

    // 🔧 Servo 2
    public void enviarServo2(String dispositivoId, int grados) {
        publicar("apiarios/dispositivos/" + dispositivoId + "/comandos/servo2", String.valueOf(grados));
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