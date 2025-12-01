package com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.LotesClient;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO.LoteResponseDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@Primary
public class LotesProxy implements ILotesService {

    private final LotesClient realService;
    private final ConcurrentMap<String, Object> cache;

    private static final long CACHE_TTL = 5 * 60 * 1000;

    public LotesProxy(LotesClient realService) {
        this.realService = realService;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public List<LoteResponseDTO> obtenerTodosLotes() {
        String cacheKey = "TODOS_LOTES";

        CacheEntry cacheEntry = getFromCache(cacheKey);
        if (cacheEntry != null && !isCacheExpired(cacheEntry)) {
            log.info("✅ [CACHE] Retornando todos los lotes desde cache");
            @SuppressWarnings("unchecked")
            List<LoteResponseDTO> cachedLotes = (List<LoteResponseDTO>) cacheEntry.getData();
            return cachedLotes;
        }

        log.info("🔍 [CACHE] Cache miss para todos los lotes, llamando al servicio real...");
        List<LoteResponseDTO> lotes = realService.obtenerTodosLotes();

        // Almacenar en cache
        cache.put(cacheKey, new CacheEntry(lotes, System.currentTimeMillis()));
        log.debug("💾 [CACHE] Todos los lotes almacenados en cache. Cantidad: {}", lotes.size());

        return lotes;
    }

    @Override
    public List<LoteResponseDTO> obtenerLotesPorAlmacen(Long idAlmacen) {
        String cacheKey = "LOTES_ALMACEN_" + idAlmacen;

        CacheEntry cacheEntry = getFromCache(cacheKey);
        if (cacheEntry != null && !isCacheExpired(cacheEntry)) {
            log.info("✅ [CACHE] Retornando lotes del almacén {} desde cache", idAlmacen);
            @SuppressWarnings("unchecked")
            List<LoteResponseDTO> cachedLotes = (List<LoteResponseDTO>) cacheEntry.getData();
            return cachedLotes;
        }

        log.info("🔍 [CACHE] Cache miss para lotes del almacén {}, llamando al servicio real...", idAlmacen);
        List<LoteResponseDTO> lotes = realService.obtenerLotesPorAlmacen(idAlmacen);

        // Almacenar en cache
        cache.put(cacheKey, new CacheEntry(lotes, System.currentTimeMillis()));
        log.debug("💾 [CACHE] Lotes del almacén {} almacenados en cache. Cantidad: {}", idAlmacen, lotes.size());

        return lotes;
    }

    @Override
    public LoteResponseDTO obtenerLotePorId(Long idLote) {
        String cacheKey = "LOTE_ID_" + idLote;

        CacheEntry cacheEntry = getFromCache(cacheKey);
        if (cacheEntry != null && !isCacheExpired(cacheEntry)) {
            log.info("✅ [CACHE] Retornando lote ID {} desde cache", idLote);
            return (LoteResponseDTO) cacheEntry.getData();
        }

        log.info("🔍 [CACHE] Cache miss para lote ID {}, llamando al servicio real...", idLote);
        LoteResponseDTO lote = realService.obtenerLotePorId(idLote);

        // Almacenar en cache
        cache.put(cacheKey, new CacheEntry(lote, System.currentTimeMillis()));
        log.debug("💾 [CACHE] Lote ID {} almacenado en cache", idLote);

        return lote;
    }

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
        return (CacheEntry) cache.get(key);
    }

    private boolean isCacheExpired(CacheEntry entry) {
        return (System.currentTimeMillis() - entry.getTimestamp()) > CACHE_TTL;
    }

    public void limpiarCacheCompleto() {
        cache.clear();
        log.info("🗑️ [CACHE] Cache de lotes limpiado completamente");
    }

    public void limpiarCachePorAlmacen(Long idAlmacen) {
        String key = "LOTES_ALMACEN_" + idAlmacen;
        cache.remove(key);
        log.info("🗑️ [CACHE] Cache limpiado para almacén {}", idAlmacen);
    }

    public void limpiarCacheLoteEspecifico(Long idLote) {
        String key = "LOTE_ID_" + idLote;
        cache.remove(key);
        log.info("🗑️ [CACHE] Cache limpiado para lote {}", idLote);
    }

    public void limpiarCacheTodosLotes() {
        cache.remove("TODOS_LOTES");
        log.info("🗑️ [CACHE] Cache limpiado para todos los lotes");
    }

    public int obtenerTamañoCache() {
        return cache.size();
    }

    public void limpiarCacheExpirado() {
        int initialSize = cache.size();
        cache.entrySet().removeIf(entry -> {
            CacheEntry cacheEntry = (CacheEntry) entry.getValue();
            return isCacheExpired(cacheEntry);
        });
        int finalSize = cache.size();
        log.info("🧹 [CACHE] Cache expirado limpiado: {} entradas removidas", (initialSize - finalSize));
    }
}