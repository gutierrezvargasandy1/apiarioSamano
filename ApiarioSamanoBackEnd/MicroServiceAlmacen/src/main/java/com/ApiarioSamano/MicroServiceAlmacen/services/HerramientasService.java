package com.ApiarioSamano.MicroServiceAlmacen.services;

import com.ApiarioSamano.MicroServiceAlmacen.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasConProveedorResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasRequest;
import com.ApiarioSamano.MicroServiceAlmacen.dto.HerramientasDTO.HerramientasResponse;
import com.ApiarioSamano.MicroServiceAlmacen.dto.ProveedoresClientMicroserviceDTO.ProveedorResponseDTO;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.model.Herramientas;
import com.ApiarioSamano.MicroServiceAlmacen.repository.AlmacenRepository;
import com.ApiarioSamano.MicroServiceAlmacen.repository.HerramientasRepository;
import com.ApiarioSamano.MicroServiceAlmacen.services.MicroServicesAPI.ProveedoresClientMicroservice;

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
public class HerramientasService {

    private final HerramientasRepository herramientasRepository;
    private final AlmacenRepository almacenRepository;
    private final ProveedoresClientMicroservice proveedoresClient;

    // ================== Mapeos ==================
    private HerramientasResponse mapHerramienta(Herramientas h) {
        HerramientasResponse response = new HerramientasResponse();
        response.setId(h.getId());
        response.setNombre(h.getNombre());

        // Convertir byte[] a Base64 String
        if (h.getFoto() != null && h.getFoto().length > 0) {
            String fotoBase64 = Base64.getEncoder().encodeToString(h.getFoto());
            response.setFoto(fotoBase64);
        } else {
            response.setFoto(null);
        }

        // ✅ CORRECCIÓN: Obtener el ID del almacén, no el objeto completo
        response.setIdAlmacen(h.getAlmacen() != null ? h.getAlmacen().getId() : null);
        response.setIdProveedor(h.getIdProveedor());

        return response;
    }

    private HerramientasConProveedorResponse mapHerramientaConProveedor(Herramientas h) {
        HerramientasConProveedorResponse response = new HerramientasConProveedorResponse();
        response.setId(h.getId());
        response.setNombre(h.getNombre());

        // Convertir byte[] a Base64 String
        if (h.getFoto() != null && h.getFoto().length > 0) {
            String fotoBase64 = Base64.getEncoder().encodeToString(h.getFoto());
            response.setFoto(fotoBase64);
        } else {
            response.setFoto(null);
        }

        // ✅ CORRECCIÓN: Agregar el ID del almacén
        response.setIdAlmacen(h.getAlmacen() != null ? h.getAlmacen().getId() : null);

        // Obtener información del proveedor
        try {
            List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
            proveedores.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(h.getIdProveedor().longValue()))
                    .findFirst()
                    .ifPresent(response::setProveedor);
        } catch (Exception e) {
            log.error("⚠️ Error al obtener proveedor para herramienta ID {}: {}", h.getId(), e.getMessage());
            response.setProveedor(null);
        }

        return response;
    }

    // ================== CRUD ==================
    @Transactional
    public CodigoResponse<HerramientasResponse> guardar(HerramientasRequest req) {
        log.info("🔍 Iniciando proceso de guardar/actualizar herramienta: {}", req.getNombre());

        // 🔹 Validar proveedor
        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        log.info("✅ Proveedores obtenidos: {} registros", proveedores.size());

        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(req.getIdProveedor().longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", req.getIdProveedor());
            return new CodigoResponse<>(404, "Proveedor no encontrado", null);
        }
        log.info("✅ Proveedor ID {} validado correctamente", req.getIdProveedor());

        // 🔹 Validar almacén
        log.info("🔍 Buscando almacén con ID: {}", req.getIdAlmacen());
        Almacen almacen = almacenRepository.findById(req.getIdAlmacen().longValue())
                .orElseThrow(() -> {
                    log.error("❌ Almacén con ID {} no encontrado", req.getIdAlmacen());
                    return new RuntimeException("Almacén no encontrado con ID: " + req.getIdAlmacen());
                });
        log.info("✅ Almacén encontrado: ID {}, Ubicación: {}", almacen.getId(), almacen.getUbicacion());

        // 🔹 Verificar capacidad del almacén solo si es una creación nueva
        if (req.getId() == null) {
            int espaciosOcupados = calcularEspaciosOcupados(almacen);
            log.info("📊 Capacidad del almacén: {}, Espacios ocupados: {}", almacen.getCapacidad(), espaciosOcupados);

            if (espaciosOcupados >= almacen.getCapacidad()) {
                log.error("❌ No hay capacidad disponible en el almacén. Capacidad: {}, Ocupados: {}",
                        almacen.getCapacidad(), espaciosOcupados);
                return new CodigoResponse<>(400, "No hay capacidad disponible en el almacén", null);
            }
        }

        Herramientas herramienta;

        // 🟡 Si viene con ID → actualizar
        if (req.getId() != null) {
            log.info("🛠️ Actualizando herramienta existente con ID: {}", req.getId());
            herramienta = herramientasRepository.findById(req.getId().longValue())
                    .orElseThrow(() -> {
                        log.error("❌ No se encontró herramienta con ID: {}", req.getId());
                        return new RuntimeException("Herramienta no encontrada con ID: " + req.getId());
                    });
        } else {
            // 🟢 Si no trae ID → crear nueva
            log.info("🆕 Creando nueva herramienta");
            herramienta = new Herramientas();
        }

        // 🔹 Asignar campos comunes
        herramienta.setNombre(req.getNombre());
        herramienta.setIdProveedor(req.getIdProveedor());
        herramienta.setAlmacen(almacen);

        // 🔹 Procesar imagen en Base64
        if (req.getFoto() != null && !req.getFoto().isEmpty()) {
            try {
                String base64Data = req.getFoto();

                // Limpiar el string Base64 - remover el prefijo "data:image/...;base64,"
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                    log.info("✅ Prefijo Base64 removido, datos limpios obtenidos");
                }

                byte[] fotoBytes = Base64.getDecoder().decode(base64Data);
                herramienta.setFoto(fotoBytes);
                log.info("✅ Foto convertida correctamente, tamaño: {} bytes", fotoBytes.length);
            } catch (IllegalArgumentException e) {
                log.error("❌ Error al decodificar Base64 de la foto: {}", e.getMessage());
                return new CodigoResponse<>(400, "Formato Base64 de la foto inválido", null);
            }
        } else {
            herramienta.setFoto(null);
            log.info("ℹ️ No se proporcionó foto para la herramienta");
        }

        // 🔹 Guardar herramienta (JPA decide si INSERT o UPDATE)
        log.info("💾 Guardando herramienta en base de datos...");
        Herramientas guardada = herramientasRepository.save(herramienta);
        log.info("✅ Herramienta {} exitosamente con ID: {}",
                req.getId() != null ? "actualizada" : "creada", guardada.getId());

        // 🔹 Si fue nueva, agregarla al almacén
        if (req.getId() == null) {
            if (almacen.getHerramientas() == null) {
                almacen.setHerramientas(new java.util.ArrayList<>());
            }
            almacen.getHerramientas().add(guardada);
            almacenRepository.save(almacen);
            log.info("✅ Herramienta agregada al almacén. Nuevos espacios ocupados: {}",
                    calcularEspaciosOcupados(almacen));
        }

        // 🔹 Respuesta final
        return new CodigoResponse<>(
                200,
                req.getId() != null ? "Herramienta actualizada correctamente" : "Herramienta guardada correctamente",
                mapHerramienta(guardada));
    }

    // Método auxiliar para calcular espacios ocupados
    private int calcularEspaciosOcupados(Almacen almacen) {
        int espacios = 0;

        // Contar herramientas
        if (almacen.getHerramientas() != null) {
            espacios += almacen.getHerramientas().size();
        }

        // Contar materias primas (si existen en tu modelo)
        if (almacen.getMateriasPrimas() != null) {
            espacios += almacen.getMateriasPrimas().size();
        }

        // Contar medicamentos (si existen en tu modelo)
        if (almacen.getMedicamentos() != null) {
            espacios += almacen.getMedicamentos().size();
        }

        return espacios;
    }

    public CodigoResponse<List<HerramientasResponse>> obtenerTodas() {
        log.info("📋 Obteniendo todas las herramientas de la base de datos");
        List<HerramientasResponse> lista = herramientasRepository.findAll()
                .stream()
                .map(this::mapHerramienta)
                .collect(Collectors.toList());
        log.info("✅ Se obtuvieron {} herramientas", lista.size());
        return new CodigoResponse<>(200, "Lista de herramientas obtenida", lista);
    }

    public CodigoResponse<HerramientasResponse> obtenerPorId(Long id) {
        log.info("🔍 Buscando herramienta con ID: {}", id);
        Optional<Herramientas> opt = herramientasRepository.findById(id);

        if (opt.isPresent()) {
            log.info("✅ Herramienta encontrada: {}", opt.get().getNombre());
            return new CodigoResponse<>(200, "Herramienta encontrada", mapHerramienta(opt.get()));
        } else {
            log.warn("⚠️ Herramienta con ID {} no encontrada", id);
            return new CodigoResponse<>(404, "Herramienta no encontrada", null);
        }
    }

    @Transactional
    public CodigoResponse<Void> eliminar(Long id) {
        log.info("🗑️ Intentando eliminar herramienta con ID: {}", id);
        Optional<Herramientas> optHerramienta = herramientasRepository.findById(id);

        if (optHerramienta.isPresent()) {
            Herramientas herramienta = optHerramienta.get();
            Almacen almacen = herramienta.getAlmacen();

            // Eliminar la herramienta
            herramientasRepository.deleteById(id);
            log.info("✅ Herramienta con ID {} eliminada correctamente", id);

            // Actualizar el almacén removiendo la herramienta
            if (almacen != null && almacen.getHerramientas() != null) {
                almacen.getHerramientas().removeIf(h -> h.getId().equals(id));
                almacenRepository.save(almacen);
                log.info("✅ Herramienta removida del almacén. Nuevos espacios ocupados: {}",
                        calcularEspaciosOcupados(almacen));
            }

            return new CodigoResponse<>(200, "Herramienta eliminada correctamente", null);
        }
        log.warn("⚠️ No se puede eliminar, herramienta con ID {} no encontrada", id);
        return new CodigoResponse<>(404, "Herramienta no encontrada", null);
    }

    // ================== MÉTODOS CON PROVEEDOR ==================
    public CodigoResponse<List<HerramientasConProveedorResponse>> obtenerTodasConProveedor() {
        log.info("📋 Obteniendo herramientas con información de proveedor");

        List<Herramientas> herramientas = herramientasRepository.findAll();
        log.info("✅ Se obtuvieron {} herramientas", herramientas.size());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        log.info("✅ Se obtuvieron {} proveedores", proveedores.size());

        List<HerramientasConProveedorResponse> resultado = herramientas.stream()
                .map(this::mapHerramientaConProveedor)
                .collect(Collectors.toList());

        log.info("✅ Herramientas con proveedor mapeadas: {} registros", resultado.size());
        return new CodigoResponse<>(200, "Herramientas con proveedor obtenidas", resultado);
    }

    public CodigoResponse<HerramientasConProveedorResponse> obtenerPorIdConProveedor(Long id) {
        log.info("🔍 Buscando herramienta con proveedor, ID: {}", id);

        Optional<Herramientas> optHerramienta = herramientasRepository.findById(id);
        if (optHerramienta.isEmpty()) {
            log.warn("⚠️ Herramienta con ID {} no encontrada", id);
            return new CodigoResponse<>(404, "Herramienta no encontrada", null);
        }

        Herramientas herramienta = optHerramienta.get();
        log.info("✅ Herramienta encontrada: {}", herramienta.getNombre());

        HerramientasConProveedorResponse dto = mapHerramientaConProveedor(herramienta);
        return new CodigoResponse<>(200, "Herramienta con proveedor obtenida", dto);
    }

    public CodigoResponse<List<HerramientasResponse>> obtenerPorProveedor(Integer idProveedor) {
        log.info("🔍 Buscando herramientas del proveedor ID: {}", idProveedor);

        log.info("🔍 Validando existencia del proveedor...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(idProveedor.longValue()));

        if (!existeProveedor) {
            log.warn("⚠️ Proveedor con ID {} no encontrado", idProveedor);
            return new CodigoResponse<>(404, "Proveedor no encontrado", List.of());
        }
        log.info("✅ Proveedor validado correctamente");

        List<HerramientasResponse> lista = herramientasRepository.findByIdProveedor(idProveedor)
                .stream()
                .map(this::mapHerramienta)
                .collect(Collectors.toList());

        log.info("✅ Se encontraron {} herramientas del proveedor {}", lista.size(), idProveedor);
        return new CodigoResponse<>(200, "Herramientas del proveedor obtenidas", lista);
    }
}