package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.AlmacenRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.AlmacenResponse;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.GeneradorCodigoClient;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.LotesClient;

import jakarta.transaction.Transactional;
import java.util.Base64;
import com.ApiarioSamano.MicroServiceAlmacen.dto.GeneradorCodigoClient.AlmacenRequestClient;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO.LoteResponseDTO;
import com.ApiarioSamano.MicroServiceAlmacen.dto.LotesClientMicroserviceDTO.ReporteEspaciosResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MedicamentosDTO.MedicamentosResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AlmacenService {

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private GeneradorCodigoClient generadorCodigoClient;

    @Autowired
    private LotesClient lotesClient;

    /**
     * Calcula y actualiza los espacios ocupados para un almacén específico
     * Incluye: materias primas + herramientas + medicamentos + lotes externos
     */
    @Transactional
    public CodigoResponse<AlmacenResponse> actualizarEspaciosOcupadosAutomaticamente(Long idAlmacen) {
        try {
            log.info("🔄 [DEBUG] INICIO - actualizarEspaciosOcupadosAutomaticamente para ID: {}", idAlmacen);

            Optional<Almacen> opt = almacenRepository.findById(idAlmacen);
            log.info("🔄 [DEBUG] Buscando almacén en BD. Resultado encontrado: {}", opt.isPresent());

            if (opt.isEmpty()) {
                log.error("❌ [DEBUG] Almacén con ID {} no encontrado en BD", idAlmacen);
                return new CodigoResponse<>(404, "Almacén no encontrado", null);
            }

            Almacen almacen = opt.get();
            log.info("🔄 [DEBUG] Almacén encontrado: ID={}, Capacidad={}", almacen.getId(), almacen.getCapacidad());

            // 1. Calcular espacios de items internos
            int espaciosInternos = calcularEspaciosOcupadosInternos(almacen);
            log.info("📦 [DEBUG] Espacios internos calculados: {}", espaciosInternos);

            // 2. Obtener y contar lotes externos que coincidan con este almacén
            int espaciosLotes = 0;
            try {
                log.info("🔄 [DEBUG] Intentando obtener lotes del microservicio...");
                List<LoteResponseDTO> lotes = lotesClient.obtenerTodosLotes();
                log.info("🔄 [DEBUG] Lotes obtenidos del microservicio. Total lotes: {}",
                        lotes != null ? lotes.size() : "null");

                if (lotes != null) {
                    // Filtrar lotes que pertenecen a este almacén
                    espaciosLotes = (int) lotes.stream()
                            .filter(lote -> lote.getIdAlmacen() != null && lote.getIdAlmacen().equals(idAlmacen))
                            .count();
                    log.info("📋 [DEBUG] Lotes externos encontrados para almacén {}: {}", idAlmacen, espaciosLotes);

                    // Log detallado de los lotes encontrados
                    if (espaciosLotes > 0) {
                        log.debug("📝 [DEBUG] Detalle de lotes encontrados:");
                        lotes.stream()
                                .filter(lote -> lote.getIdAlmacen() != null && lote.getIdAlmacen().equals(idAlmacen))
                                .forEach(lote -> log.debug("   - Lote ID: {}, Código: {}, Producto: {}",
                                        lote.getId(), lote.getNumeroSeguimiento(), lote.getTipoProducto()));
                    }
                } else {
                    log.warn("⚠️ [DEBUG] Lotes obtenidos es NULL");
                }
            } catch (Exception e) {
                log.error("❌ [DEBUG] ERROR al obtener lotes externos: {}", e.getMessage(), e);
                log.warn("⚠️ [DEBUG] No se pudieron obtener lotes externos: {}. Continuando solo con espacios internos",
                        e.getMessage());
                espaciosLotes = 0;
            }

            // 3. Calcular total
            int totalEspaciosOcupados = espaciosInternos + espaciosLotes;
            log.info("📊 [DEBUG] Resumen final - Almacén ID: {}, Internos: {}, Lotes: {}, Total: {}, Capacidad: {}",
                    idAlmacen, espaciosInternos, espaciosLotes, totalEspaciosOcupados, almacen.getCapacidad());

            // 4. Validar capacidad
            if (totalEspaciosOcupados > almacen.getCapacidad()) {
                log.warn("🚨 [DEBUG] Capacidad excedida - Total: {}, Capacidad: {}", totalEspaciosOcupados,
                        almacen.getCapacidad());
            }

            // 5. Actualizar el almacén
            log.info("🔄 [DEBUG] Guardando almacén en BD...");
            Almacen almacenActualizado = almacenRepository.save(almacen);
            log.info("🔄 [DEBUG] Almacén guardado exitosamente");

            log.info("🔄 [DEBUG] Mapeando a response...");
            AlmacenResponse response = mapToResponse(almacenActualizado);
            log.info("🔄 [DEBUG] Mapeo completado exitosamente");

            log.info("✅ [DEBUG] FIN - actualizarEspaciosOcupadosAutomaticamente completado exitosamente");
            return new CodigoResponse<>(200,
                    String.format("Espacios ocupados actualizados. Internos: %d, Lotes: %d, Total: %d",
                            espaciosInternos, espaciosLotes, totalEspaciosOcupados),
                    response);

        } catch (Exception e) {
            log.error("❌ [DEBUG] ERROR CRÍTICO en actualizarEspaciosOcupadosAutomaticamente: {}", e.getMessage(), e);
            log.error("❌ [DEBUG] Stack trace completo:", e);
            return new CodigoResponse<>(500, "Error al actualizar espacios ocupados: " + e.getMessage(), null);
        }
    }

    /**
     * Método auxiliar para calcular espacios ocupados por items internos
     */
    private int calcularEspaciosOcupadosInternos(Almacen almacen) {
        log.info("🔄 [DEBUG] INICIO - calcularEspaciosOcupadosInternos");
        int espacios = 0;

        if (almacen.getMateriasPrimas() != null) {
            espacios += almacen.getMateriasPrimas().size();
            log.info("📦 [DEBUG] Materias primas count: {}", almacen.getMateriasPrimas().size());
        } else {
            log.info("📦 [DEBUG] Materias primas es NULL");
        }

        if (almacen.getHerramientas() != null) {
            espacios += almacen.getHerramientas().size();
            log.info("🛠️ [DEBUG] Herramientas count: {}", almacen.getHerramientas().size());
        } else {
            log.info("🛠️ [DEBUG] Herramientas es NULL");
        }

        if (almacen.getMedicamentos() != null) {
            espacios += almacen.getMedicamentos().size();
            log.info("💊 [DEBUG] Medicamentos count: {}", almacen.getMedicamentos().size());
        } else {
            log.info("💊 [DEBUG] Medicamentos es NULL");
        }

        log.info("✅ [DEBUG] FIN - calcularEspaciosOcupadosInternos. Total: {}", espacios);
        return espacios;
    }

    /**
     * Método para obtener el reporte completo de espacios ocupados
     */
    public CodigoResponse<ReporteEspaciosResponse> obtenerReporteEspaciosOcupados(Long idAlmacen) {
        try {
            log.info("📋 [DEBUG] INICIO - obtenerReporteEspaciosOcupados para ID: {}", idAlmacen);

            Optional<Almacen> opt = almacenRepository.findById(idAlmacen);
            log.info("📋 [DEBUG] Buscando almacén en BD. Resultado: {}", opt.isPresent());

            if (opt.isEmpty()) {
                log.error("❌ [DEBUG] Almacén no encontrado para reporte ID: {}", idAlmacen);
                return new CodigoResponse<>(404, "Almacén no encontrado", null);
            }

            Almacen almacen = opt.get();
            log.info("📋 [DEBUG] Almacén encontrado: ID={}, Capacidad={}", almacen.getId(), almacen.getCapacidad());

            // Calcular espacios internos
            int materiasPrimas = almacen.getMateriasPrimas() != null ? almacen.getMateriasPrimas().size() : 0;
            int herramientas = almacen.getHerramientas() != null ? almacen.getHerramientas().size() : 0;
            int medicamentos = almacen.getMedicamentos() != null ? almacen.getMedicamentos().size() : 0;
            int espaciosInternos = materiasPrimas + herramientas + medicamentos;

            log.info("📋 [DEBUG] Espacios internos - MP: {}, Herramientas: {}, Medicamentos: {}, Total: {}",
                    materiasPrimas, herramientas, medicamentos, espaciosInternos);

            // Obtener lotes externos
            int lotesExternos = 0;
            List<LoteResponseDTO> detalleLotes = new ArrayList<>();

            try {
                log.info("📋 [DEBUG] Obteniendo lotes externos para reporte...");
                List<LoteResponseDTO> lotes = lotesClient.obtenerTodosLotes();
                log.info("📋 [DEBUG] Lotes obtenidos para reporte: {}", lotes != null ? lotes.size() : "null");

                if (lotes != null) {
                    detalleLotes = lotes.stream()
                            .filter(lote -> lote.getIdAlmacen() != null && lote.getIdAlmacen().equals(idAlmacen))
                            .collect(Collectors.toList());
                    lotesExternos = detalleLotes.size();
                    log.info("📋 [DEBUG] Lotes filtrados para almacén {}: {}", idAlmacen, lotesExternos);
                }
            } catch (Exception e) {
                log.error("❌ [DEBUG] ERROR obteniendo lotes para reporte: {}", e.getMessage(), e);
                log.warn("⚠️ [DEBUG] No se pudieron obtener lotes externos para el reporte: {}", e.getMessage());
            }

            int totalEspacios = espaciosInternos + lotesExternos;
            int espaciosDisponibles = Math.max(0, almacen.getCapacidad() - totalEspacios);

            log.info("📋 [DEBUG] Cálculos finales - Total: {}, Disponibles: {}, Capacidad: {}",
                    totalEspacios, espaciosDisponibles, almacen.getCapacidad());

            // Crear respuesta del reporte
            log.info("📋 [DEBUG] Creando objeto ReporteEspaciosResponse...");
            ReporteEspaciosResponse reporte = new ReporteEspaciosResponse();
            reporte.setAlmacenId(idAlmacen);
            reporte.setCapacidadTotal(almacen.getCapacidad());
            reporte.setEspaciosInternos(espaciosInternos);
            reporte.setMateriasPrimas(materiasPrimas);
            reporte.setHerramientas(herramientas);
            reporte.setMedicamentos(medicamentos);
            reporte.setLotesExternos(lotesExternos);
            reporte.setTotalEspaciosOcupados(totalEspacios);
            reporte.setEspaciosDisponibles(espaciosDisponibles);
            reporte.setDetalleLotes(detalleLotes);
            reporte.setPorcentajeOcupacion((double) totalEspacios / almacen.getCapacidad() * 100);

            log.info("📊 [DEBUG] Reporte generado - Total ocupados: {}/{}, Disponibles: {}, %Ocupación: {}",
                    totalEspacios, almacen.getCapacidad(), espaciosDisponibles, reporte.getPorcentajeOcupacion());

            log.info("✅ [DEBUG] FIN - obtenerReporteEspaciosOcupados completado exitosamente");
            return new CodigoResponse<>(200, "Reporte de espacios generado correctamente", reporte);

        } catch (Exception e) {
            log.error("❌ [DEBUG] ERROR CRÍTICO en obtenerReporteEspaciosOcupados: {}", e.getMessage(), e);
            log.error("❌ [DEBUG] Stack trace completo:", e);
            return new CodigoResponse<>(500, "Error al generar reporte: " + e.getMessage(), null);
        }
    }

    // Crear o actualizar
    public CodigoResponse<AlmacenResponse> guardarAlmacen(AlmacenRequest almacenRequest) {
        try {
            log.info("🔄 [DEBUG] INICIO - guardarAlmacen");
            log.info("🔄 [DEBUG] Datos recibidos - Ubicación: {}, Capacidad: {}",
                    almacenRequest.getUbicacion(), almacenRequest.getCapacidad());

            log.info("🔄 [DEBUG] Solicitando código al microservicio de generador de códigos...");
            String codigoResponse = generadorCodigoClient.generarAlmacen(
                    new AlmacenRequestClient(almacenRequest.getUbicacion(), "AlmacenGeneral"));
            log.info("🔄 [DEBUG] Código generado recibido del microservicio: {}", codigoResponse);

            Almacen nuevoAlmacen = new Almacen();
            nuevoAlmacen.setNumeroSeguimiento(codigoResponse);
            nuevoAlmacen.setUbicacion(almacenRequest.getUbicacion());
            nuevoAlmacen.setCapacidad(almacenRequest.getCapacidad());

            // Inicializar las listas como vacías para evitar null
            nuevoAlmacen.setHerramientas(new ArrayList<>());
            nuevoAlmacen.setMateriasPrimas(new ArrayList<>());
            nuevoAlmacen.setMedicamentos(new ArrayList<>());

            log.info("🔄 [DEBUG] Almacén creado. Guardando en BD...");

            Almacen almacenGuardado = almacenRepository.save(nuevoAlmacen);
            log.info("🔄 [DEBUG] Almacén guardado correctamente con ID: {}", almacenGuardado.getId());

            log.info("🔄 [DEBUG] Mapeando a response...");
            AlmacenResponse response = mapToResponse(almacenGuardado);
            log.info("🔄 [DEBUG] Mapeo completado");

            log.info("✅ [DEBUG] FIN - guardarAlmacen completado exitosamente");
            return new CodigoResponse<>(200, "Almacén guardado correctamente", response);

        } catch (Exception e) {
            log.error("❌ [DEBUG] ERROR CRÍTICO en guardarAlmacen: {}", e.getMessage(), e);
            log.error("❌ [DEBUG] Stack trace completo:", e);
            return new CodigoResponse<>(500, "Error al guardar el almacén: " + e.getMessage(), null);
        }
    }

    // Obtener todos
    @Transactional
    public CodigoResponse<List<AlmacenResponse>> obtenerTodos() {
        try {
            log.info("🔄 [DEBUG] INICIO - obtenerTodos");

            log.info("🔄 [DEBUG] Buscando todos los almacenes en BD...");
            List<Almacen> almacenes = almacenRepository.findAll();
            log.info("🔄 [DEBUG] Almacenes encontrados en BD: {}", almacenes.size());

            log.info("🔄 [DEBUG] Mapeando almacenes a response...");
            List<AlmacenResponse> lista = almacenes.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            log.info("🔄 [DEBUG] Mapeo completado. Responses creados: {}", lista.size());

            log.info("✅ [DEBUG] FIN - obtenerTodos completado exitosamente");
            return new CodigoResponse<>(200, "Lista de almacenes obtenida", lista);

        } catch (Exception e) {
            log.error("❌ [DEBUG] ERROR CRÍTICO en obtenerTodos: {}", e.getMessage(), e);
            log.error("❌ [DEBUG] Stack trace completo:", e);
            return new CodigoResponse<>(500, "Error al obtener almacenes: " + e.getMessage(), null);
        }
    }

    // Obtener por ID
    @Transactional
    public CodigoResponse<AlmacenResponse> obtenerPorId(Long id) {
        try {
            log.info("🔄 [DEBUG] INICIO - obtenerPorId para ID: {}", id);

            log.info("🔄 [DEBUG] Buscando almacén en BD...");
            Optional<Almacen> opt = almacenRepository.findById(id);
            log.info("🔄 [DEBUG] Resultado búsqueda: {}", opt.isPresent());

            if (opt.isPresent()) {
                log.info("🔄 [DEBUG] Almacén encontrado. Mapeando a response...");
                AlmacenResponse response = mapToResponse(opt.get());
                log.info("✅ [DEBUG] FIN - obtenerPorId completado exitosamente");
                return new CodigoResponse<>(200, "Almacén encontrado", response);
            }

            log.error("❌ [DEBUG] Almacén no encontrado para ID: {}", id);
            return new CodigoResponse<>(404, "Almacén no encontrado", null);

        } catch (Exception e) {
            log.error("❌ [DEBUG] ERROR CRÍTICO en obtenerPorId: {}", e.getMessage(), e);
            log.error("❌ [DEBUG] Stack trace completo:", e);
            return new CodigoResponse<>(500, "Error al obtener almacén: " + e.getMessage(), null);
        }
    }

    // Eliminar por ID
    public CodigoResponse<Void> eliminarPorId(Long id) {
        try {
            log.info("🔄 [DEBUG] INICIO - eliminarPorId para ID: {}", id);

            log.info("🔄 [DEBUG] Verificando existencia del almacén...");
            boolean existe = almacenRepository.existsById(id);
            log.info("🔄 [DEBUG] Almacén existe: {}", existe);

            if (existe) {
                log.info("🔄 [DEBUG] Eliminando almacén...");
                almacenRepository.deleteById(id);
                log.info("✅ [DEBUG] FIN - eliminarPorId completado exitosamente");
                return new CodigoResponse<>(200, "Almacén eliminado correctamente", null);
            }

            log.error("❌ [DEBUG] Almacén no encontrado para eliminar ID: {}", id);
            return new CodigoResponse<>(404, "Almacén no encontrado", null);

        } catch (Exception e) {
            log.error("❌ [DEBUG] ERROR CRÍTICO en eliminarPorId: {}", e.getMessage(), e);
            log.error("❌ [DEBUG] Stack trace completo:", e);
            return new CodigoResponse<>(500, "Error al eliminar almacén: " + e.getMessage(), null);
        }
    }

    /**
     * Método auxiliar para convertir byte[] a String Base64
     */
    private String convertBytesToBase64(byte[] fotoBytes) {
        try {
            log.debug("🔄 [DEBUG] INICIO - convertBytesToBase64");

            if (fotoBytes == null) {
                log.debug("📷 [DEBUG] Foto bytes es NULL");
                return null;
            }
            if (fotoBytes.length == 0) {
                log.debug("📷 [DEBUG] Foto bytes está vacío");
                return null;
            }

            log.debug("📷 [DEBUG] Convirtiendo {} bytes a Base64", fotoBytes.length);
            String base64 = Base64.getEncoder().encodeToString(fotoBytes);
            log.debug("📷 [DEBUG] Conversión completada. Longitud Base64: {}", base64.length());

            return base64;

        } catch (Exception e) {
            log.error("❌ [DEBUG] ERROR en convertBytesToBase64: {}", e.getMessage(), e);
            return null;
        }
    }

    // Mapeo de entidad a Response (ÚNICO método mapToResponse)
    private AlmacenResponse mapToResponse(Almacen a) {
        try {
            log.debug("🔄 [DEBUG] INICIO - mapToResponse para Almacén ID: {}", a.getId());

            AlmacenResponse response = new AlmacenResponse();
            response.setId(a.getId());
            response.setNumeroSeguimiento(a.getNumeroSeguimiento());
            response.setUbicacion(a.getUbicacion());
            response.setCapacidad(a.getCapacidad());

            log.debug("🔄 [DEBUG] Procesando materias primas...");
            // Materias primas - manejar lista nula
            List<MateriasPrimasResponse> materias = a.getMateriasPrimas() != null
                    ? a.getMateriasPrimas().stream()
                            .map(m -> {
                                log.debug("📦 [DEBUG] Mapeando materia prima ID: {}", m.getId());
                                MateriasPrimasResponse materiaResponse = new MateriasPrimasResponse();
                                materiaResponse.setId(m.getId());
                                materiaResponse.setNombre(m.getNombre());
                                // Convertir byte[] a String Base64
                                materiaResponse.setFoto(convertBytesToBase64(m.getFoto()));
                                materiaResponse.setCantidad(m.getCantidad());
                                materiaResponse.setIdProveedor(m.getIdProveedor());
                                return materiaResponse;
                            })
                            .collect(Collectors.toList())
                    : new ArrayList<>();
            response.setMateriasPrimas(materias);
            log.debug("📦 [DEBUG] Materias primas mapeadas: {}", materias.size());

            log.debug("🔄 [DEBUG] Procesando herramientas...");
            // Herramientas - manejar lista nula
            List<HerramientasResponse> herramientas = a.getHerramientas() != null
                    ? a.getHerramientas().stream()
                            .map(h -> {
                                log.debug("🛠️ [DEBUG] Mapeando herramienta ID: {}", h.getId());
                                HerramientasResponse herramientaResponse = new HerramientasResponse();
                                herramientaResponse.setId(h.getId());
                                herramientaResponse.setNombre(h.getNombre());
                                // Convertir byte[] a String Base64
                                herramientaResponse.setFoto(convertBytesToBase64(h.getFoto()));
                                herramientaResponse.setIdProveedor(h.getIdProveedor());
                                return herramientaResponse;
                            })
                            .collect(Collectors.toList())
                    : new ArrayList<>();
            response.setHerramientas(herramientas);
            log.debug("🛠️ [DEBUG] Herramientas mapeadas: {}", herramientas.size());

            log.debug("🔄 [DEBUG] Procesando medicamentos...");
            // Medicamentos - manejar lista nula
            List<MedicamentosResponse> medicamentos = a.getMedicamentos() != null
                    ? a.getMedicamentos().stream()
                            .map(m -> {
                                log.debug("💊 [DEBUG] Mapeando medicamento ID: {}", m.getId());
                                MedicamentosResponse medicamentoResponse = new MedicamentosResponse();
                                medicamentoResponse.setId(m.getId());
                                medicamentoResponse.setNombre(m.getNombre());
                                medicamentoResponse.setCantidad(m.getCantidad());
                                medicamentoResponse.setDescripcion(m.getDescripcion());
                                // Convertir byte[] a String Base64
                                medicamentoResponse.setFoto(convertBytesToBase64(m.getFoto()));
                                medicamentoResponse.setIdProveedor(m.getIdProveedor());
                                return medicamentoResponse;
                            })
                            .collect(Collectors.toList())
                    : new ArrayList<>();
            response.setMedicamentos(medicamentos);
            log.debug("💊 [DEBUG] Medicamentos mapeadas: {}", medicamentos.size());

            log.debug("✅ [DEBUG] FIN - mapToResponse completado exitosamente");
            return response;

        } catch (Exception e) {
            log.error("❌ [DEBUG] ERROR CRÍTICO en mapToResponse: {}", e.getMessage(), e);
            log.error("❌ [DEBUG] Stack trace completo:", e);
            throw e; // Relanzar la excepción para que se capture en el método llamador
        }
    }
}