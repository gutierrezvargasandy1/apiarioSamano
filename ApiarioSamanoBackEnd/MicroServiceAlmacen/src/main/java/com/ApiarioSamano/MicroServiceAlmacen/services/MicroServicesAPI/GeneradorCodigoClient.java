package com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ApiarioSamano.MicroServiceAlmacen.config.JwtTokenProvider;
import com.ApiarioSamano.MicroServiceAlmacen.dto.GeneradorCodigoClient.AlmacenRequestClient;
import com.ApiarioSamano.MicroServiceAlmacen.dto.GeneradorCodigoClient.CodigoResponseDTO;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GeneradorCodigoClient {

    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${microservicio.generador.base-url}")
    private String baseUrl;

    public GeneradorCodigoClient(RestTemplate restTemplate, JwtTokenProvider jwtTokenProvider) {
        this.restTemplate = restTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private HttpHeaders createHeadersWithJwt(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);

        String jwt = jwtTokenProvider.getCurrentJwtToken();
        if (jwt != null) {
            headers.set("Authorization", "Bearer " + jwt);
            log.debug("JWT agregado a headers: {}", jwt);
        } else {
            log.warn("No se encontró JWT en el request actual");
        }

        return headers;
    }

    public String generarAlmacen(AlmacenRequestClient request) {
        String url = baseUrl + "/almacen";
        log.info("Preparando request al microservicio de códigos: URL={}, Request={}", url, request);

        HttpEntity<AlmacenRequestClient> requestEntity = new HttpEntity<>(request,
                createHeadersWithJwt(MediaType.APPLICATION_JSON));

        ResponseEntity<CodigoResponseDTO> response = null;

        try {
            log.debug("Enviando request al microservicio...");
            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, CodigoResponseDTO.class);
            log.info("Request enviado. Código de estado HTTP: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Error al llamar al microservicio de códigos", e);
            throw e;
        }

        CodigoResponseDTO body = response.getBody();
        if (body == null) {
            log.error("El microservicio devolvió un body nulo");
            throw new IllegalStateException("El microservicio devolvió un body nulo");
        }

        log.debug("Respuesta recibida del microservicio: {}", body);

        log.info("Código generado: {}, Estatus: {}, Descripción: {}", body.getCodigo(), body.getEstatus(),
                body.getDescripcion());
        return body.getCodigo();
    }
}
