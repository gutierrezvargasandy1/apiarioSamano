package com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.GeneradorCodigosClient;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import com.ApiarioSamano.MicroServiceAlmacen.dto.GeneradorCodigoClient.AlmacenRequestClient;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@Primary
public class GeneradorCodigoProxy implements IGeneradorCodigoService {

    private final GeneradorCodigoClient realService;
    private final ConcurrentMap<String, String> cache;

    public GeneradorCodigoProxy(GeneradorCodigoClient realService) {
        this.realService = realService;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public String generarAlmacen(AlmacenRequestClient request) {
        String cacheKey = generateCacheKey(request);

        if (cache.containsKey(cacheKey)) {
            log.info("Retornando resultado desde cache para: {}", cacheKey);
            return cache.get(cacheKey);
        }

        log.info("Cache miss, llamando al servicio real...");
        String resultado = realService.generarAlmacen(request);

        cache.put(cacheKey, resultado);
        log.debug("Resultado almacenado en cache");

        return resultado;
    }

    private String generateCacheKey(AlmacenRequestClient request) {
        return request.toString().hashCode() + "_key";
    }

    public void limpiarCache() {
        cache.clear();
        log.info("Cache limpiado");
    }

    public int obtenerTamañoCache() {
        return cache.size();
    }
}