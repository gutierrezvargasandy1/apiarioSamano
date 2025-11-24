package com.ApiarioSamano.MicroServiceProduccion.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@Log4j2
public class OllamaConfig {

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Bean
    public WebClient ollamaWebClient() {
        log.info("🔧 Configurando WebClient para Ollama: {}", ollamaUrl);

        // ✅ 1. CONFIGURAR HTTP CLIENT CON TIMEOUTS APROPIADOS
        HttpClient httpClient = HttpClient.create()
                // Timeout de conexión: 10 segundos
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                // Timeout de respuesta: 90 segundos (para modelos lentos)
                .responseTimeout(Duration.ofSeconds(300))
                // Configurar handlers de timeout para lectura/escritura
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(300, TimeUnit.SECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(300, TimeUnit.SECONDS));
                })
                // Log de conexiones para debugging
                .doOnConnected(conn -> log.debug("✅ Conexión establecida con Ollama"))
                .doOnDisconnected(conn -> log.debug("🔌 Desconectado de Ollama"));

        // ✅ 2. AUMENTAR LÍMITE DE MEMORIA PARA RESPUESTAS GRANDES
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)) // 16MB buffer
                .build();

        // ✅ 3. CREAR WEBCLIENT CON TODAS LAS CONFIGURACIONES
        WebClient client = WebClient.builder()
                .baseUrl(ollamaUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                // ✅ 4. AGREGAR FILTROS DE LOG PARA DEBUGGING
                .filter(logRequest())
                .filter(logResponse())
                .build();

        log.info("✅ WebClient para Ollama configurado exitosamente");
        log.info("   URL: {}", ollamaUrl);
        log.info("   Timeout conexión: 10s");
        log.info("   Timeout respuesta: 90s");
        log.info("   Buffer: 16MB");

        return client;
    }

    // ✅ FILTRO PARA LOGGEAR REQUESTS (DEBUGGING)
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("📤 Request: {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.headers().forEach(
                        (name, values) -> values.forEach(value -> log.debug("   Header: {} = {}", name, value)));
            }
            return Mono.just(clientRequest);
        });
    }

    // ✅ FILTRO PARA LOGGEAR RESPONSES (DEBUGGING)
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("📥 Response: Status {}", clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }
}