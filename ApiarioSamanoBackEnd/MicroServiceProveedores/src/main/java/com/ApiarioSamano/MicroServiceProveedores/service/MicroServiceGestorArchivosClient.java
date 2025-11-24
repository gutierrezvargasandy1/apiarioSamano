package com.ApiarioSamano.MicroServiceProveedores.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.ApiarioSamano.MicroServiceProveedores.config.JwtTokenProvider;
import com.ApiarioSamano.MicroServiceProveedores.dto.ClientMicroServiceGestorArchivosDTO.FileUploadResponse;

import java.io.IOException;

@Component
public class MicroServiceGestorArchivosClient {

    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${microservicio.archivos.url}")
    private String baseUrl;

    public MicroServiceGestorArchivosClient(RestTemplate restTemplate, JwtTokenProvider jwtTokenProvider) {
        this.restTemplate = restTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private HttpHeaders createHeadersWithJwt(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        String jwt = jwtTokenProvider.getCurrentJwtToken();
        if (jwt != null) {
            headers.set("Authorization", "Bearer " + jwt);
        }
        return headers;
    }

    public FileUploadResponse uploadFile(MultipartFile file) throws IOException {
        String url = baseUrl + "/api/files/upload";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body,
                createHeadersWithJwt(MediaType.MULTIPART_FORM_DATA));

        ResponseEntity<FileUploadResponse> response = restTemplate.postForEntity(url, requestEntity,
                FileUploadResponse.class);
        return response.getBody();
    }

    public Resource downloadFile(String id) {
        String url = baseUrl + "/api/files/download/" + id;

        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeadersWithJwt(MediaType.APPLICATION_OCTET_STREAM));
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Resource.class);
        return response.getBody();
    }

    public Resource viewFile(String id) {
        String url = baseUrl + "/api/ver/view/" + id;

        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeadersWithJwt(MediaType.APPLICATION_OCTET_STREAM));
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Resource.class);
        return response.getBody();
    }

    public void deleteFile(String id) {
        String url = baseUrl + "/api/files/" + id;

        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeadersWithJwt(MediaType.APPLICATION_JSON));
        restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
    }

    public void updateFilePhysical(String id, MultipartFile file) throws IOException {
        String url = baseUrl + "/api/files/update/" + id;

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body,
                createHeadersWithJwt(MediaType.MULTIPART_FORM_DATA));

        restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
    }

    /** Helper para enviar MultipartFile con RestTemplate */
    static class MultipartInputStreamFileResource extends ByteArrayResource {
        private final String filename;

        public MultipartInputStreamFileResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        public MultipartInputStreamFileResource(java.io.InputStream inputStream, String filename) throws IOException {
            super(inputStream.readAllBytes());
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }
}
