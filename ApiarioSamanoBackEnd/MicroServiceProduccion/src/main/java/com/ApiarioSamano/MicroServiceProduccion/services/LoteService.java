package com.ApiarioSamano.MicroServiceProduccion.services;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.LoteDTO.LoteConAlmacenResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.LoteDTO.LoteRequest;
import com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientAlmaceneDTO.AlmacenResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.MicroServiceClientGeneradorCoigoDTO.LoteRequestClient;
import com.ApiarioSamano.MicroServiceProduccion.model.Lote;
import com.ApiarioSamano.MicroServiceProduccion.repository.LoteRepository;
import com.ApiarioSamano.MicroServiceProduccion.services.MicroServiceClientAlmacen.AlmacenClientMicroservice;
import com.ApiarioSamano.MicroServiceProduccion.services.MicroServiceClientGeneradorCodigo.CodigoClientMicroservice;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private final AlmacenClientMicroservice almacenClientMicroservice;

    @Autowired
    private final CodigoClientMicroservice codigoClientMicroservice;

    private static final Logger log = LoggerFactory.getLogger(LoteService.class);

    // Guardar lote (crear o editar)
    public CodigoResponse<Lote> guardarLote(LoteRequest request) {
        try {
            log.info("Validando existencia del almacén con ID: {}", request.getIdAlmacen());

            // 🔹 Verificar que el almacén exista
            var responseAlmacen = almacenClientMicroservice.obtenerAlmacenPorId(request.getIdAlmacen());
            if (responseAlmacen == null || responseAlmacen.getCodigo() != 200 || responseAlmacen.getData() == null) {
                log.warn("El almacén con ID {} no existe o no fue encontrado", request.getIdAlmacen());
                return new CodigoResponse<>(404, "El almacén especificado no existe", null);
            }

            // 🔹 VERIFICAR SI ES EDICIÓN (request tiene ID)
            if (request.getId() != null && request.getId() > 0) {
                // 🔹 MODO EDICIÓN - Buscar lote existente
                Optional<Lote> loteExistenteOpt = loteRepository.findById(request.getId());
                if (loteExistenteOpt.isPresent()) {
                    Lote loteExistente = loteExistenteOpt.get();

                    // 🔹 Actualizar datos del lote existente
                    loteExistente.setTipoProducto(request.getTipoProducto());
                    loteExistente.setIdAlmacen(request.getIdAlmacen());

                    // 🔹 Guardar cambios
                    Lote loteActualizado = loteRepository.save(loteExistente);
                    log.info("Lote actualizado con ID: {}", loteActualizado.getId());

                    return new CodigoResponse<>(200, "Lote actualizado correctamente", loteActualizado);
                } else {
                    log.warn("Lote con ID {} no encontrado para edición", request.getId());
                    return new CodigoResponse<>(404, "Lote no encontrado", null);
                }
            } else {
                // 🔹 MODO CREACIÓN - Crear nuevo lote
                // Crear lote temporal con número de seguimiento temporal
                Lote loteTemporal = new Lote();
                loteTemporal.setNumeroSeguimiento("TEMP-" + System.currentTimeMillis()); // Temporal
                loteTemporal.setTipoProducto(request.getTipoProducto());
                loteTemporal.setFechaCreacion(LocalDate.now());
                loteTemporal.setIdAlmacen(request.getIdAlmacen());

                // 🔹 Guardar para obtener ID
                Lote loteGuardado = loteRepository.save(loteTemporal);
                log.info("Lote temporal guardado con ID: {}", loteGuardado.getId());

                // 🔹 Generar código real con el ID
                LoteRequestClient infLote = new LoteRequestClient();
                infLote.setProducto(request.getTipoProducto());
                infLote.setNumeroLote(loteGuardado.getId().intValue());

                String codigoSeguimientoReal = codigoClientMicroservice.generarLote(infLote);

                if (codigoSeguimientoReal == null || codigoSeguimientoReal.trim().isEmpty()) {
                    // Si falla, mantener el temporal
                    log.warn("No se pudo generar código real, manteniendo temporal");
                    return new CodigoResponse<>(200, "Lote guardado con código temporal", loteGuardado);
                }

                // 🔹 Actualizar con código real
                loteGuardado.setNumeroSeguimiento(codigoSeguimientoReal);
                Lote loteFinal = loteRepository.save(loteGuardado);

                log.info("Lote actualizado con código real: {}", loteFinal.getNumeroSeguimiento());
                return new CodigoResponse<>(200, "Lote guardado correctamente", loteFinal);
            }

        } catch (Exception e) {
            log.error("Error al guardar el lote: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al guardar el lote: " + e.getMessage(), null);
        }
    }

    public CodigoResponse<List<Lote>> listarLotes() {
        List<Lote> lista = loteRepository.findAll();
        return new CodigoResponse<>(200, "Listado de lotes obtenido correctamente", lista);
    }

    /**
     * 🔹 Obtiene un lote por su ID junto con la información del almacén desde otro
     * microservicio.
     */
    public CodigoResponse<LoteConAlmacenResponse> obtenerLoteConAlmacenPorId(Long idLote) {
        try {
            log.info("Buscando lote con ID: {}", idLote);

            Optional<Lote> optionalLote = loteRepository.findById(idLote);

            if (optionalLote.isEmpty()) {
                log.warn("No se encontró el lote con ID {}", idLote);
                return new CodigoResponse<>(404, "Lote no encontrado", null);
            }

            Lote lote = optionalLote.get();
            AlmacenResponse almacenResponse = null;

            try {
                var almacenResponseData = almacenClientMicroservice.obtenerAlmacenPorId(lote.getIdAlmacen());
                if (almacenResponseData.getCodigo() == 200) {
                    almacenResponse = almacenResponseData.getData();
                } else {
                    log.warn("El microservicio de almacén no devolvió datos válidos para el ID {}",
                            lote.getIdAlmacen());
                }
            } catch (Exception e) {
                log.error("Error al obtener información del almacén para el lote {}: {}", lote.getId(), e.getMessage());
            }

            LoteConAlmacenResponse respuesta = new LoteConAlmacenResponse(
                    lote.getId(),
                    lote.getNumeroSeguimiento(),
                    lote.getTipoProducto(),
                    lote.getFechaCreacion(),
                    lote.getIdAlmacen(),
                    almacenResponse);

            return new CodigoResponse<>(200, "Lote con almacén obtenido correctamente", respuesta);

        } catch (Exception e) {
            log.error("Error al obtener lote con almacén: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al obtener el lote con su almacén", null);
        }
    }

    public CodigoResponse<Void> eliminarLote(Long id) {
        if (!loteRepository.existsById(id)) {
            return new CodigoResponse<>(404, "Lote no encontrado", null);
        }

        loteRepository.deleteById(id);
        return new CodigoResponse<>(200, "Lote eliminado correctamente", null);
    }
}