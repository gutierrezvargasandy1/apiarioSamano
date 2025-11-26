package com.ApiarioSamano.MicroServiceApiarios.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class OllamaConfig {

    @Bean
    public WebClient ollamaWebClient(@Value("${ollama.url:http://ollama:11434}") String ollamaUrl) {

        // Configurar HttpClient con timeouts
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(120));

        return WebClient.builder()
                .baseUrl(ollamaUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)) // 16MB buffer
                .build();
    }
}