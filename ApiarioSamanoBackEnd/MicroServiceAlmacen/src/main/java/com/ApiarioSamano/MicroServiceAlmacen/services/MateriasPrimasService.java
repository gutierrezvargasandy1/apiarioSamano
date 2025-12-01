package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.AlmacenDTO.AlmacenResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasConProveedorDTO;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.MateriasPrimasDTO.MateriasPrimasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceAlmacen.model.MateriasPrimas;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;
import com.ApiarioSamano.MicroServiceAlmacen.repository.MateriasPrimasRepository;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.ProveedoresClient.IProveedoresService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MateriasPrimasService {

    private final MateriasPrimasRepository materiasPrimasRepository;

    private final IProveedoresService proveedoresService;

    private final AlmacenRepository almacenRepository;

    private AlmacenResponse mapAlmacen(Almacen almacen) {
        if (almacen == null)
            return null;

        AlmacenResponse response = new AlmacenResponse();
        response.setId(almacen.getId());
        response.setNumeroSeguimiento(almacen.getNumeroSeguimiento());
        response.setUbicacion(almacen.getUbicacion());
        response.setCapacidad(almacen.getCapacidad());

        return response;
    }

    private MateriasPrimasResponse mapMateria(MateriasPrimas m) {
        MateriasPrimasResponse response = new MateriasPrimasResponse();
        response.setId(m.getId());
        response.setNombre(m.getNombre());

        if (m.getFoto() != null && m.getFoto().length > 0) {
            String fotoBase64 = Base64.getEncoder().encodeToString(m.getFoto());
            response.setFoto(fotoBase64);
        } else {
            response.setFoto(null);
        }

        response.setCantidad(m.getCantidad());
        response.setIdProveedor(m.getIdProveedor());

        return response;
    }

    private byte[] convertBase64ToBytes(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            return null;
        }

        try {
            if (base64String.contains(",")) {
                base64String = base64String.split(",")[1];
            }

            return Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            log.error("❌ Error al decodificar Base64: {}", e.getMessage());
            throw new RuntimeException("Formato Base64 inválido");
        }
    }

    @Transactional
    public CodigoResponse<MateriasPrimasResponse> guardar(MateriasPrimasRequest req) {
        log.info("🔍 Guardando materia prima: {}", req.getNombre());

        // ✅ Si trae ID → actualizar
        MateriasPrimas materia;
        if (req.getId() != null) {
            log.info("✏️ Modo actualización ID: {}", req.getId());

            materia = materiasPrimasRepository.findById(req.getId())
                    .orElseThrow(() -> new RuntimeException("Materia prima no encontrada"));

        } else {
            log.info("🆕 Modo creación");
            materia = new MateriasPrimas();
        }

        log.info("🔍 [CACHE-PROVEEDORES] Validando proveedor ID: {} (con cache)...", req.getIdProvedor());
        List<ProveedorResponseDTO> proveedores = proveedoresService.obtenerTodosProveedores();

        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(req.getIdProvedor().longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", req.getIdProvedor());
            return new CodigoResponse<>(404, "Proveedor no encontrado", null);
        }
        log.info("✅ Proveedor ID {} validado correctamente", req.getIdProvedor());

        Almacen almacen = almacenRepository.findById(req.getIdAlmacen())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));

        if (req.getId() == null) {
            int espaciosOcupados = calcularEspaciosOcupados(almacen);
            log.info("📊 Capacidad del almacén: {}, Espacios ocupados: {}", almacen.getCapacidad(), espaciosOcupados);

            if (espaciosOcupados >= almacen.getCapacidad()) {
                log.error("❌ No hay capacidad disponible en el almacén. Capacidad: {}, Ocupados: {}",
                        almacen.getCapacidad(), espaciosOcupados);
                return new CodigoResponse<>(400, "No hay capacidad disponible en el almacén", null);
            }
        }

        materia.setNombre(req.getNombre());
        materia.setCantidad(req.getCantidad());
        materia.setAlmacen(almacen);
        materia.setIdProveedor(req.getIdProvedor());

        if (req.getFoto() != null && !req.getFoto().isBlank()) {
            materia.setFoto(convertBase64ToBytes(req.getFoto()));
        }

        log.info("💾 Guardando en BD...");
        MateriasPrimas guardada = materiasPrimasRepository.save(materia);

        if (req.getId() == null) {
            if (almacen.getMateriasPrimas() == null) {
                almacen.setMateriasPrimas(new java.util.ArrayList<>());
            }
            almacen.getMateriasPrimas().add(guardada);
            almacenRepository.save(almacen);
            log.info("✅ Materia prima agregada al almacén. Nuevos espacios ocupados: {}",
                    calcularEspaciosOcupados(almacen));
        }

        return new CodigoResponse<>(
                200,
                "✅ Materia prima " + (req.getId() != null ? "actualizada" : "creada") + " correctamente",
                mapMateria(guardada));
    }

    private int calcularEspaciosOcupados(Almacen almacen) {
        int espacios = 0;

        if (almacen.getMateriasPrimas() != null) {
            espacios += almacen.getMateriasPrimas().size();
        }

        if (almacen.getHerramientas() != null) {
            espacios += almacen.getHerramientas().size();
        }

        if (almacen.getMedicamentos() != null) {
            espacios += almacen.getMedicamentos().size();
        }

        return espacios;
    }

    public CodigoResponse<List<MateriasPrimasResponse>> obtenerTodas() {
        log.info("📋 Obteniendo todas las materias primas de la base de datos");
        List<MateriasPrimasResponse> lista = materiasPrimasRepository.findAll()
                .stream()
                .map(this::mapMateria)
                .collect(Collectors.toList());
        log.info("✅ Se obtuvieron {} materias primas", lista.size());
        return new CodigoResponse<>(200, "Lista de materias primas obtenida", lista);
    }

    public CodigoResponse<MateriasPrimasResponse> obtenerPorId(Long id) {
        log.info("🔍 Buscando materia prima con ID: {}", id);
        Optional<MateriasPrimas> opt = materiasPrimasRepository.findById(id);

        if (opt.isPresent()) {
            log.info("✅ Materia prima encontrada: {}", opt.get().getNombre());
            return new CodigoResponse<>(200, "Materia prima encontrada", mapMateria(opt.get()));
        } else {
            log.warn("⚠️ Materia prima con ID {} no encontrada", id);
            return new CodigoResponse<>(404, "Materia prima no encontrada", null);
        }
    }

    @Transactional
    public CodigoResponse<Void> eliminarPorId(Long id) {
        log.info("🗑️ Intentando eliminar materia prima con ID: {}", id);
        Optional<MateriasPrimas> optMateria = materiasPrimasRepository.findById(id);

        if (optMateria.isPresent()) {
            MateriasPrimas materia = optMateria.get();
            Almacen almacen = materia.getAlmacen();

            materiasPrimasRepository.deleteById(id);
            log.info("✅ Materia prima con ID {} eliminada correctamente", id);

            // Actualizar el almacén removiendo la materia prima
            if (almacen != null && almacen.getMateriasPrimas() != null) {
                almacen.getMateriasPrimas().removeIf(m -> m.getId().equals(id));
                almacenRepository.save(almacen);
                log.info("✅ Materia prima removida del almacén. Nuevos espacios ocupados: {}",
                        calcularEspaciosOcupados(almacen));
            }

            return new CodigoResponse<>(200, "Materia prima eliminada correctamente", null);
        }
        log.warn("⚠️ No se puede eliminar, materia prima con ID {} no encontrada", id);
        return new CodigoResponse<>(404, "Materia prima no encontrada", null);
    }

    // ================== MÉTODOS CON PROVEEDOR ==================
    public CodigoResponse<List<MateriasPrimasConProveedorDTO>> obtenerTodasConProveedor() {
        log.info("📋 Obteniendo materias primas con información de proveedor (con Proxy/Cache)");

        List<MateriasPrimas> materias = materiasPrimasRepository.findAll();
        log.info("✅ Se obtuvieron {} materias primas", materias.size());

        log.info("🔍 [CACHE-PROVEEDORES] Consultando microservicio de proveedores (con cache)...");
        List<ProveedorResponseDTO> proveedores = proveedoresService.obtenerTodosProveedores();
        log.info("✅ [CACHE-PROVEEDORES] Se obtuvieron {} proveedores", proveedores.size());

        List<MateriasPrimasConProveedorDTO> resultado = materias.stream().map(m -> {
            MateriasPrimasConProveedorDTO dto = new MateriasPrimasConProveedorDTO();
            dto.setId(m.getId());
            dto.setNombre(m.getNombre());

            // Convertir byte[] a String Base64
            if (m.getFoto() != null && m.getFoto().length > 0) {
                String fotoBase64 = Base64.getEncoder().encodeToString(m.getFoto());
                dto.setFoto(fotoBase64);
            } else {
                dto.setFoto(null);
            }

            dto.setCantidad(m.getCantidad());
            dto.setAlmacen(mapAlmacen(m.getAlmacen()));

            // Buscar proveedor en la lista cacheada
            proveedores.stream()
                    .filter(p -> p.getId().equals(m.getIdProveedor().longValue()))
                    .findFirst()
                    .ifPresent(proveedor -> {
                        dto.setProveedor(proveedor);
                        log.debug("✅ [CACHE-PROVEEDORES] Proveedor encontrado: {}", proveedor.getNombreEmpresa());
                    });

            return dto;
        }).collect(Collectors.toList());

        log.info("✅ Materias primas con proveedor mapeadas: {} registros", resultado.size());
        return new CodigoResponse<>(200, "Materias primas con proveedor obtenidas", resultado);
    }

    public CodigoResponse<MateriasPrimasConProveedorDTO> obtenerPorIdConProveedor(Long id) {
        log.info("🔍 Buscando materia prima con proveedor, ID: {} (con Proxy/Cache)", id);

        Optional<MateriasPrimas> optMateria = materiasPrimasRepository.findById(id);
        if (optMateria.isEmpty()) {
            log.warn("⚠️ Materia prima con ID {} no encontrada", id);
            return new CodigoResponse<>(404, "Materia prima no encontrada", null);
        }

        MateriasPrimas materia = optMateria.get();
        log.info("✅ Materia prima encontrada: {}", materia.getNombre());

        log.info("🔍 [CACHE-PROVEEDORES] Consultando microservicio de proveedores (con cache)...");
        List<ProveedorResponseDTO> proveedores = proveedoresService.obtenerTodosProveedores();

        MateriasPrimasConProveedorDTO dto = new MateriasPrimasConProveedorDTO();
        dto.setId(materia.getId());
        dto.setNombre(materia.getNombre());

        // Convertir byte[] a String Base64
        if (materia.getFoto() != null && materia.getFoto().length > 0) {
            String fotoBase64 = Base64.getEncoder().encodeToString(materia.getFoto());
            dto.setFoto(fotoBase64);
        } else {
            dto.setFoto(null);
        }

        dto.setCantidad(materia.getCantidad());
        dto.setAlmacen(mapAlmacen(materia.getAlmacen()));

        proveedores.stream()
                .filter(p -> p.getId().equals(materia.getIdProveedor().longValue()))
                .findFirst()
                .ifPresent(proveedor -> {
                    dto.setProveedor(proveedor);
                    log.info("✅ [CACHE-PROVEEDORES] Proveedor asociado: {}", proveedor.getNombreEmpresa());
                });

        return new CodigoResponse<>(200, "Materia prima con proveedor obtenida", dto);
    }

    public CodigoResponse<List<MateriasPrimasResponse>> obtenerPorAlmacen(Almacen almacen) {
        log.info("🔍 Buscando materias primas del almacén ID: {}", almacen.getId());
        List<MateriasPrimasResponse> lista = materiasPrimasRepository.findByAlmacen(almacen)
                .stream()
                .map(this::mapMateria)
                .collect(Collectors.toList());
        log.info("✅ Se encontraron {} materias primas en el almacén", lista.size());
        return new CodigoResponse<>(200, "Materias primas del almacén obtenidas", lista);
    }

    public CodigoResponse<List<MateriasPrimasResponse>> obtenerPorProveedor(Integer idProveedor) {
        log.info("🔍 Buscando materias primas del proveedor ID: {} (con Proxy/Cache)", idProveedor);

        log.info("🔍 [CACHE-PROVEEDORES] Validando existencia del proveedor...");
        List<ProveedorResponseDTO> proveedores = proveedoresService.obtenerTodosProveedores();
        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(idProveedor.longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", idProveedor);
            return new CodigoResponse<>(404, "Proveedor no encontrado", List.of());
        }
        log.info("✅ Proveedor validado correctamente");

        List<MateriasPrimasResponse> lista = materiasPrimasRepository.findByIdProveedor(idProveedor)
                .stream()
                .map(this::mapMateria)
                .collect(Collectors.toList());

        log.info("✅ Se encontraron {} materias primas del proveedor {}", lista.size(), idProveedor);
        return new CodigoResponse<>(200, "Materias primas del proveedor obtenidas", lista);
    }

    /**
     * Método para obtener materias primas con información completa (almacén y
     * proveedor)
     */
    public CodigoResponse<List<MateriasPrimasConProveedorDTO>> obtenerMateriasPrimasCompletas() {
        log.info("📋 Obteniendo materias primas con información completa (almacén + proveedor)");

        List<MateriasPrimas> materias = materiasPrimasRepository.findAll();
        log.info("✅ Se obtuvieron {} materias primas", materias.size());

        // PROXY: Una sola llamada cacheada para todos los proveedores
        log.info("🔍 [CACHE-PROVEEDORES] Obteniendo proveedores (con cache)...");
        List<ProveedorResponseDTO> proveedores = proveedoresService.obtenerTodosProveedores();
        log.info("✅ [CACHE-PROVEEDORES] Proveedores obtenidos: {}", proveedores.size());

        List<MateriasPrimasConProveedorDTO> resultado = materias.stream()
                .map(materia -> {
                    MateriasPrimasConProveedorDTO dto = new MateriasPrimasConProveedorDTO();
                    dto.setId(materia.getId());
                    dto.setNombre(materia.getNombre());
                    dto.setCantidad(materia.getCantidad());

                    // Convertir foto a Base64
                    if (materia.getFoto() != null && materia.getFoto().length > 0) {
                        dto.setFoto(Base64.getEncoder().encodeToString(materia.getFoto()));
                    }

                    // Información del almacén
                    dto.setAlmacen(mapAlmacen(materia.getAlmacen()));

                    // Buscar proveedor en la lista cacheada
                    proveedores.stream()
                            .filter(p -> p.getId() != null && p.getId().equals(materia.getIdProveedor().longValue()))
                            .findFirst()
                            .ifPresent(dto::setProveedor);

                    return dto;
                })
                .collect(Collectors.toList());

        log.info("✅ Materias primas completas procesadas: {} registros", resultado.size());
        return new CodigoResponse<>(200, "Materias primas con información completa obtenidas", resultado);
    }

    /**
     * Método para obtener materias primas por almacén con información de proveedor
     */
    public CodigoResponse<List<MateriasPrimasConProveedorDTO>> obtenerPorAlmacenConProveedor(Long idAlmacen) {
        log.info("🔍 Buscando materias primas del almacén {} con información de proveedor (con Proxy/Cache)",
                idAlmacen);

        Optional<Almacen> optAlmacen = almacenRepository.findById(idAlmacen);
        if (optAlmacen.isEmpty()) {
            log.warn("⚠️ Almacén con ID {} no encontrado", idAlmacen);
            return new CodigoResponse<>(404, "Almacén no encontrado", List.of());
        }

        Almacen almacen = optAlmacen.get();
        List<MateriasPrimas> materias = materiasPrimasRepository.findByAlmacen(almacen);
        log.info("✅ Se encontraron {} materias primas en el almacén {}", materias.size(), idAlmacen);

        // PROXY: Una sola llamada cacheada para todos los proveedores
        log.info("🔍 [CACHE-PROVEEDORES] Obteniendo proveedores (con cache)...");
        List<ProveedorResponseDTO> proveedores = proveedoresService.obtenerTodosProveedores();

        List<MateriasPrimasConProveedorDTO> resultado = materias.stream()
                .map(materia -> {
                    MateriasPrimasConProveedorDTO dto = new MateriasPrimasConProveedorDTO();
                    dto.setId(materia.getId());
                    dto.setNombre(materia.getNombre());
                    dto.setCantidad(materia.getCantidad());

                    // Convertir foto a Base64
                    if (materia.getFoto() != null && materia.getFoto().length > 0) {
                        dto.setFoto(Base64.getEncoder().encodeToString(materia.getFoto()));
                    }

                    // Información del almacén
                    dto.setAlmacen(mapAlmacen(almacen));

                    // Buscar proveedor en la lista cacheada
                    proveedores.stream()
                            .filter(p -> p.getId() != null && p.getId().equals(materia.getIdProveedor().longValue()))
                            .findFirst()
                            .ifPresent(dto::setProveedor);

                    return dto;
                })
                .collect(Collectors.toList());

        log.info("✅ Materias primas del almacén con proveedor procesadas: {} registros", resultado.size());
        return new CodigoResponse<>(200, "Materias primas del almacén con proveedor obtenidas", resultado);
    }
}