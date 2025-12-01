package com.ApiarioSamano.MicroServiceApiarios.service.MicroServicesAPI.MedicamentosServiceClient;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import com.ApiarioSamano.MicroServiceApiarios.dto.MedicamentosDTO.MedicamentosResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@Primary
public class MedicamentosProxy implements IMedicamentosService {

    private final MicroServiceClientMedicamentos realService;
    private final ConcurrentMap<String, CacheEntry> cache;

    // Tiempo de vida del cache en milisegundos (5 minutos - los medicamentos pueden
    // cambiar)
    private static final long CACHE_TTL = 5 * 60 * 1000;

    public MedicamentosProxy(MicroServiceClientMedicamentos realService) {
        this.realService = realService;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public List<MedicamentosResponse> obtenerTodos() {
        String cacheKey = "TODOS_MEDICAMENTOS";

        CacheEntry cacheEntry = getFromCache(cacheKey);
        if (cacheEntry != null && !isCacheExpired(cacheEntry)) {
            log.info("✅ [CACHE-MEDICAMENTOS] Retornando todos los medicamentos desde cache");
            @SuppressWarnings("unchecked")
            List<MedicamentosResponse> cachedMedicamentos = (List<MedicamentosResponse>) cacheEntry.getData();
            return cachedMedicamentos;
        }

        log.info("🔍 [CACHE-MEDICAMENTOS] Cache miss para todos los medicamentos, llamando al servicio real...");
        List<MedicamentosResponse> medicamentos = realService.obtenerTodos();

        // Almacenar en cache solo si se obtuvieron medicamentos
        if (medicamentos != null && !medicamentos.isEmpty()) {
            cache.put(cacheKey, new CacheEntry(medicamentos, System.currentTimeMillis()));
            log.debug("💾 [CACHE-MEDICAMENTOS] Medicamentos almacenados en cache. Cantidad: {}", medicamentos.size());
        } else {
            log.warn("⚠️ [CACHE-MEDICAMENTOS] No se almacenó en cache porque no se obtuvieron medicamentos");
        }

        return medicamentos;
    }

    @Override
    public MedicamentosResponse obtenerPorId(Long id) {
        String cacheKey = "MEDICAMENTO_ID_" + id;

        CacheEntry cacheEntry = getFromCache(cacheKey);
        if (cacheEntry != null && !isCacheExpired(cacheEntry)) {
            log.info("✅ [CACHE-MEDICAMENTOS] Retornando medicamento ID {} desde cache", id);
            return (MedicamentosResponse) cacheEntry.getData();
        }

        log.info("🔍 [CACHE-MEDICAMENTOS] Cache miss para medicamento ID {}, llamando al servicio real...", id);
        MedicamentosResponse medicamento = realService.obtenerPorId(id);

        // Almacenar en cache
        cache.put(cacheKey, new CacheEntry(medicamento, System.currentTimeMillis()));
        log.debug("💾 [CACHE-MEDICAMENTOS] Medicamento ID {} almacenado en cache", id);

        return medicamento;
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
        log.info("🗑️ [CACHE-MEDICAMENTOS] Cache de medicamentos limpiado completamente");
    }

    public void limpiarCachePorId(Long id) {
        String key = "MEDICAMENTO_ID_" + id;
        cache.remove(key);
        log.info("🗑️ [CACHE-MEDICAMENTOS] Cache limpiado para medicamento ID {}", id);
    }

    public void limpiarCacheTodos() {
        cache.remove("TODOS_MEDICAMENTOS");
        log.info("🗑️ [CACHE-MEDICAMENTOS] Cache limpiado para todos los medicamentos");
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
        log.info("🧹 [CACHE-MEDICAMENTOS] Cache expirado limpiado: {} entradas removidas", (initialSize - finalSize));
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