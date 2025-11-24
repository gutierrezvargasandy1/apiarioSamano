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
public class MateriasPrimasService {

    private final MateriasPrimasRepository materiasPrimasRepository;
    private final ProveedoresClientMicroservice proveedoresClient;
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

    /**
     * Convierte byte[] a String Base64 para la respuesta
     */
    private MateriasPrimasResponse mapMateria(MateriasPrimas m) {
        MateriasPrimasResponse response = new MateriasPrimasResponse();
        response.setId(m.getId());
        response.setNombre(m.getNombre());

        // Convertir byte[] a String Base64
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

    /**
     * Convierte String Base64 a byte[] para guardar en BD
     */
    private byte[] convertBase64ToBytes(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            return null;
        }

        try {
            // Si viene con prefijo data:image/...;base64, lo removemos
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

        // Validar proveedor
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        boolean existeProveedor = proveedores.stream()
                .anyMatch(p -> p.getId().equals(req.getIdProvedor().longValue()));

        if (!existeProveedor) {
            return new CodigoResponse<>(404, "Proveedor no encontrado", null);
        }

        // Buscar almacén
        Almacen almacen = almacenRepository.findById(req.getIdAlmacen())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));

        materia.setNombre(req.getNombre());
        materia.setCantidad(req.getCantidad());
        materia.setAlmacen(almacen);
        materia.setIdProveedor(req.getIdProvedor());

        // Foto base64 a byte[]
        if (req.getFoto() != null && !req.getFoto().isBlank()) {
            materia.setFoto(convertBase64ToBytes(req.getFoto()));
        }

        log.info("💾 Guardando en BD...");
        MateriasPrimas guardada = materiasPrimasRepository.save(materia);

        return new CodigoResponse<>(
                200,
                "✅ Materia prima " + (req.getId() != null ? "actualizada" : "creada") + " correctamente",
                mapMateria(guardada));
    }

    // Método auxiliar para calcular espacios ocupados
    private int calcularEspaciosOcupados(Almacen almacen) {
        int espacios = 0;

        // Contar materias primas
        if (almacen.getMateriasPrimas() != null) {
            espacios += almacen.getMateriasPrimas().size();
        }

        // Contar herramientas (si existen en tu modelo)
        if (almacen.getHerramientas() != null) {
            espacios += almacen.getHerramientas().size();
        }

        // Contar medicamentos (si existen en tu modelo)
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

            // Eliminar la materia prima
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
        log.info("📋 Obteniendo materias primas con información de proveedor");

        List<MateriasPrimas> materias = materiasPrimasRepository.findAll();
        log.info("✅ Se obtuvieron {} materias primas", materias.size());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
        log.info("✅ Se obtuvieron {} proveedores", proveedores.size());

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

            proveedores.stream()
                    .filter(p -> p.getId().equals(m.getIdProveedor().longValue()))
                    .findFirst()
                    .ifPresent(dto::setProveedor);

            return dto;
        }).collect(Collectors.toList());

        log.info("✅ Materias primas con proveedor mapeadas: {} registros", resultado.size());
        return new CodigoResponse<>(200, "Materias primas con proveedor obtenidas", resultado);
    }

    public CodigoResponse<MateriasPrimasConProveedorDTO> obtenerPorIdConProveedor(Long id) {
        log.info("🔍 Buscando materia prima con proveedor, ID: {}", id);

        Optional<MateriasPrimas> optMateria = materiasPrimasRepository.findById(id);
        if (optMateria.isEmpty()) {
            log.warn("⚠️ Materia prima con ID {} no encontrada", id);
            return new CodigoResponse<>(404, "Materia prima no encontrada", null);
        }

        MateriasPrimas materia = optMateria.get();
        log.info("✅ Materia prima encontrada: {}", materia.getNombre());

        log.info("🔍 Consultando microservicio de proveedores...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();

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
                    log.info("✅ Proveedor asociado: {}", proveedor.getNombreEmpresa());
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
        log.info("🔍 Buscando materias primas del proveedor ID: {}", idProveedor);

        log.info("🔍 Validando existencia del proveedor...");
        List<ProveedorResponseDTO> proveedores = proveedoresClient.obtenerTodosProveedores();
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

}