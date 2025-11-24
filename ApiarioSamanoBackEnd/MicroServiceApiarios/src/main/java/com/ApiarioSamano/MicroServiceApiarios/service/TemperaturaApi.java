package com.ApiarioSamano.MicroServiceApiarios.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemperaturaApi {

    @Value("${openmeteo.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public TemperaturaApi() {
        var requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // 5 segundos
        requestFactory.setReadTimeout(5000); // 5 segundos

        this.restTemplate = new RestTemplate(requestFactory);
    }

    public double obtenerTemperaturaActualDolores() {
        try {
            double lat = 21.155;
            double lon = -100.938;

            String urlFormateada = String.format(apiUrl, lat, lon);

            String json = restTemplate.getForObject(urlFormateada, String.class);

            JSONObject obj = new JSONObject(json);
            JSONObject current = obj.getJSONObject("current_weather");
            return current.getDouble("temperature");

        } catch (Exception e) {
            log.warn("⚠️ No se pudo obtener temperatura actual, usando valor por defecto. Error: {}", e.getMessage());
            return 25.0;
        }
    }
}
