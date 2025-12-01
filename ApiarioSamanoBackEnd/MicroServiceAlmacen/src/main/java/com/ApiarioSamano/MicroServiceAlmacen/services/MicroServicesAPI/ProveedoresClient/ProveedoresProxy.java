package com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.ProveedoresClient;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@Primary
public class ProveedoresProxy implements IProveedoresService {

    private final ProveedoresClientMicroservice realService;
    private final ConcurrentMap<String, CacheEntry> cache;

    // Tiempo de vida del cache en milisegundos (10 minutos - los proveedores
    // cambian menos frecuentemente)
    private static final long CACHE_TTL = 10 * 60 * 1000;

    public ProveedoresProxy(ProveedoresClientMicroservice realService) {
        this.realService = realService;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public List<ProveedorResponseDTO> obtenerTodosProveedores() {
        String cacheKey = "TODOS_PROVEEDORES";

        CacheEntry cacheEntry = getFromCache(cacheKey);
        if (cacheEntry != null && !isCacheExpired(cacheEntry)) {
            log.info("✅ [CACHE-PROVEEDORES] Retornando todos los proveedores desde cache");
            @SuppressWarnings("unchecked")
            List<ProveedorResponseDTO> cachedProveedores = (List<ProveedorResponseDTO>) cacheEntry.getData();
            return cachedProveedores;
        }

        log.info("🔍 [CACHE-PROVEEDORES] Cache miss para todos los proveedores, llamando al servicio real...");
        List<ProveedorResponseDTO> proveedores = realService.obtenerTodosProveedores();

        // Almacenar en cache solo si se obtuvieron proveedores
        if (proveedores != null && !proveedores.isEmpty()) {
            cache.put(cacheKey, new CacheEntry(proveedores, System.currentTimeMillis()));
            log.debug("💾 [CACHE-PROVEEDORES] Proveedores almacenados en cache. Cantidad: {}", proveedores.size());
        } else {
            log.warn("⚠️ [CACHE-PROVEEDORES] No se almacenó en cache porque no se obtuvieron proveedores");
        }

        return proveedores;
    }

    /**
     * Clase interna para manejar entradas de cache con timestamp
     */
    private static class CacheEntry {
        private final Object data;
        private final long timestamp;

        public CacheEntry(Object data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }

        public Object getData() {
            return data;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private CacheEntry getFromCache(String key) {
        return cache.get(key);
    }

    private boolean isCacheExpired(CacheEntry entry) {
        return (System.currentTimeMillis() - entry.getTimestamp()) > CACHE_TTL;
    }

    /**
     * Métodos para gestión del cache
     */

    public void limpiarCacheCompleto() {
        cache.clear();
        log.info("🗑️ [CACHE-PROVEEDORES] Cache de proveedores limpiado completamente");
    }

    public int obtenerTamañoCache() {
        return cache.size();
    }

    public void limpiarCacheExpirado() {
        int initialSize = cache.size();
        cache.entrySet().removeIf(entry -> {
            CacheEntry cacheEntry = entry.getValue();
            return isCacheExpired(cacheEntry);
        });
        int finalSize = cache.size();
        log.info("🧹 [CACHE-PROVEEDORES] Cache expirado limpiado: {} entradas removidas", (initialSize - finalSize));
    }

    public boolean isCacheActivo(String key) {
        CacheEntry entry = cache.get(key);
        return entry != null && !isCacheExpired(entry);
    }

    public long getTiempoRestanteCache(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || isCacheExpired(entry)) {
            return 0;
        }
        return CACHE_TTL - (System.currentTimeMillis() - entry.getTimestamp());
    }
}