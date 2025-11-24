package com.ApiarioSamano.MicroServiceProduccion.services;

import com.ApiarioSamano.MicroServiceProduccion.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProduccion.dto.ProductoDTO.ProductoRequest;
import com.ApiarioSamano.MicroServiceProduccion.dto.ProductoDTO.ProductoResponse;
import com.ApiarioSamano.MicroServiceProduccion.model.Lote;
import com.ApiarioSamano.MicroServiceProduccion.model.Producto;
import com.ApiarioSamano.MicroServiceProduccion.repository.LoteRepository;
import com.ApiarioSamano.MicroServiceProduccion.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    @Transactional
    public CodigoResponse<Producto> crearProducto(ProductoRequest request) {
        try {
            log.info("Creando producto: {} para lote ID: {}", request.getNombre(), request.getIdLote());

            // 🔹 Validar que el lote exista
            Optional<Lote> loteOptional = loteRepository.findById(request.getIdLote());
            if (loteOptional.isEmpty()) {
                log.warn("Lote con ID {} no encontrado", request.getIdLote());
                return new CodigoResponse<>(404, "El lote especificado no existe", null);
            }

            Lote lote = loteOptional.get();

            // 🔹 VALIDACIÓN CRÍTICA: El tipo de producto debe ser IDÉNTICO al tipo del lote
            if (!validarTipoProductoConLote(request.getTipoDeProducto(), lote.getTipoProducto())) {
                log.warn("Tipo de producto no coincide con el tipo del lote. Producto: {}, Lote: {}",
                        request.getTipoDeProducto(), lote.getTipoProducto());
                return new CodigoResponse<>(400,
                        String.format(
                                "El tipo de producto '%s' no coincide con el tipo del lote '%s'. Deben ser idénticos.",
                                request.getTipoDeProducto(), lote.getTipoProducto()),
                        null);
            }

            // 🔹 Validar código de barras único
            if (request.getCodigoBarras() != null) {
                if (productoRepository.existsByCodigoBarras(request.getCodigoBarras())) {
                    log.warn("Código de barras ya existe: {}", request.getCodigoBarras());
                    return new CodigoResponse<>(400, "El código de barras ya existe", null);
                }

                // Validar que no exista el mismo código en el mismo lote
                if (productoRepository.existsByCodigoBarrasAndLoteId(request.getCodigoBarras(), request.getIdLote())) {
                    log.warn("Código de barras {} ya existe en el lote {}", request.getCodigoBarras(),
                            request.getIdLote());
                    return new CodigoResponse<>(400, "El código de barras ya existe en este lote", null);
                }
            }

            // 🔹 Crear producto
            Producto producto = new Producto();
            producto.setNombre(request.getNombre());
            producto.setPrecioMayoreo(request.getPrecioMayoreo());
            producto.setPrecioMenudeo(request.getPrecioMenudeo());
            producto.setFoto(request.getFoto());
            producto.setCodigoBarras(request.getCodigoBarras());
            producto.setTipoDeProducto(request.getTipoDeProducto());
            producto.setLote(lote);
            producto.setActivo(request.getActivo() != null ? request.getActivo() : true);

            Producto guardado = productoRepository.save(producto);
            log.info("Producto creado correctamente con ID: {}", guardado.getId());

            return new CodigoResponse<>(200, "Producto creado correctamente", guardado);

        } catch (Exception e) {
            log.error("Error al crear producto: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al crear producto: " + e.getMessage(), null);
        }
    }

    @Transactional
    public CodigoResponse<Producto> actualizarProducto(Long id, ProductoRequest request) {
        try {
            log.info("Actualizando producto ID: {}", id);

            Optional<Producto> productoOptional = productoRepository.findById(id);
            if (productoOptional.isEmpty()) {
                return new CodigoResponse<>(404, "Producto no encontrado", null);
            }

            Producto producto = productoOptional.get();

            // 🔹 Si se cambia el lote, validar que el tipo de producto coincida con el
            // nuevo lote
            if (request.getIdLote() != null && !request.getIdLote().equals(producto.getLote().getId())) {
                Optional<Lote> nuevoLoteOptional = loteRepository.findById(request.getIdLote());
                if (nuevoLoteOptional.isEmpty()) {
                    return new CodigoResponse<>(404, "El nuevo lote especificado no existe", null);
                }

                Lote nuevoLote = nuevoLoteOptional.get();

                // 🔹 VALIDACIÓN: El tipo de producto debe coincidir con el nuevo lote
                String tipoProducto = request.getTipoDeProducto() != null ? request.getTipoDeProducto()
                        : producto.getTipoDeProducto();

                if (!validarTipoProductoConLote(tipoProducto, nuevoLote.getTipoProducto())) {
                    return new CodigoResponse<>(400,
                            String.format("El tipo de producto '%s' no coincide con el tipo del nuevo lote '%s'.",
                                    tipoProducto, nuevoLote.getTipoProducto()),
                            null);
                }

                producto.setLote(nuevoLote);
            }

            // 🔹 Si se actualiza el tipo de producto, validar que coincida con el lote
            // actual
            if (request.getTipoDeProducto() != null &&
                    !request.getTipoDeProducto().equals(producto.getTipoDeProducto())) {

                if (!validarTipoProductoConLote(request.getTipoDeProducto(), producto.getLote().getTipoProducto())) {
                    return new CodigoResponse<>(400,
                            String.format("El nuevo tipo de producto '%s' no coincide con el tipo del lote '%s'.",
                                    request.getTipoDeProducto(), producto.getLote().getTipoProducto()),
                            null);
                }
            }

            // 🔹 Validar código de barras único (excluyendo el actual)
            if (request.getCodigoBarras() != null &&
                    !request.getCodigoBarras().equals(producto.getCodigoBarras()) &&
                    productoRepository.existsByCodigoBarrasAndIdNot(request.getCodigoBarras(), id)) {
                return new CodigoResponse<>(400, "El código de barras ya existe", null);
            }

            // 🔹 Actualizar campos
            if (request.getNombre() != null)
                producto.setNombre(request.getNombre());
            if (request.getPrecioMayoreo() != null)
                producto.setPrecioMayoreo(request.getPrecioMayoreo());
            if (request.getPrecioMenudeo() != null)
                producto.setPrecioMenudeo(request.getPrecioMenudeo());
            if (request.getFoto() != null)
                producto.setFoto(request.getFoto());
            if (request.getCodigoBarras() != null)
                producto.setCodigoBarras(request.getCodigoBarras());
            if (request.getTipoDeProducto() != null)
                producto.setTipoDeProducto(request.getTipoDeProducto());
            if (request.getActivo() != null)
                producto.setActivo(request.getActivo());

            Producto actualizado = productoRepository.save(producto);
            return new CodigoResponse<>(200, "Producto actualizado correctamente", actualizado);

        } catch (Exception e) {
            log.error("Error al actualizar producto: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al actualizar producto: " + e.getMessage(), null);
        }
    }

    /**
     * 🔹 VALIDACIÓN: Compara si el tipo de producto es IDÉNTICO al tipo de producto
     * del lote
     * Usa la misma lógica flexible que en el servicio de Cosechas
     */
    private boolean validarTipoProductoConLote(String tipoProducto, String tipoLote) {
        if (tipoProducto == null || tipoLote == null) {
            return false;
        }

        // 🔹 OPCIÓN 1: Comparación EXACTA (case-sensitive)
        // return tipoProducto.equals(tipoLote);

        // 🔹 OPCIÓN 2: Comparación EXACTA ignorando mayúsculas (recomendada)
        // return tipoProducto.equalsIgnoreCase(tipoLote);

        // 🔹 OPCIÓN 3: Comparación FLEXIBLE (elimina espacios y normaliza)
        String productoNormalizado = normalizarTexto(tipoProducto);
        String loteNormalizado = normalizarTexto(tipoLote);
        return productoNormalizado.equals(loteNormalizado);
    }

    /**
     * 🔹 Normaliza el texto para comparación flexible
     * - Convierte a minúsculas
     * - Elimina espacios extras
     * - Normaliza caracteres especiales
     */
    private String normalizarTexto(String texto) {
        if (texto == null)
            return "";

        return texto.toLowerCase()
                .trim()
                .replaceAll("\\s+", " ") // Elimina múltiples espacios
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n");
    }

    public CodigoResponse<List<ProductoResponse>> listarProductosActivos() {
        try {
            List<Producto> productos = productoRepository.findByActivoTrue();
            List<ProductoResponse> response = productos.stream()
                    .map(this::convertirAProductoResponse)
                    .collect(Collectors.toList());

            return new CodigoResponse<>(200, "Productos activos obtenidos correctamente", response);
        } catch (Exception e) {
            log.error("Error al listar productos: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al listar productos", null);
        }
    }

    public CodigoResponse<ProductoResponse> obtenerProductoPorId(Long id) {
        try {
            Optional<Producto> productoOptional = productoRepository.findById(id);
            if (productoOptional.isEmpty()) {
                return new CodigoResponse<>(404, "Producto no encontrado", null);
            }

            ProductoResponse response = convertirAProductoResponse(productoOptional.get());
            return new CodigoResponse<>(200, "Producto obtenido correctamente", response);
        } catch (Exception e) {
            log.error("Error al obtener producto: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al obtener producto", null);
        }
    }

    public CodigoResponse<List<ProductoResponse>> obtenerProductosPorLote(Long idLote) {
        try {
            List<Producto> productos = productoRepository.findByLoteId(idLote);
            List<ProductoResponse> response = productos.stream()
                    .map(this::convertirAProductoResponse)
                    .collect(Collectors.toList());

            return new CodigoResponse<>(200, "Productos del lote obtenidos correctamente", response);
        } catch (Exception e) {
            log.error("Error al obtener productos del lote: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al obtener productos del lote", null);
        }
    }

    public CodigoResponse<Void> desactivarProducto(Long id) {
        try {
            Optional<Producto> productoOptional = productoRepository.findById(id);
            if (productoOptional.isEmpty()) {
                return new CodigoResponse<>(404, "Producto no encontrado", null);
            }

            Producto producto = productoOptional.get();
            producto.setActivo(false);
            productoRepository.save(producto);

            return new CodigoResponse<>(200, "Producto desactivado correctamente", null);
        } catch (Exception e) {
            log.error("Error al desactivar producto: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al desactivar producto", null);
        }
    }

    // Método de conversión
    private ProductoResponse convertirAProductoResponse(Producto producto) {
        ProductoResponse response = new ProductoResponse();
        response.setId(producto.getId());
        response.setNombre(producto.getNombre());
        response.setPrecioMayoreo(producto.getPrecioMayoreo());
        response.setPrecioMenudeo(producto.getPrecioMenudeo());
        response.setCodigoBarras(producto.getCodigoBarras());
        response.setTipoDeProducto(producto.getTipoDeProducto());
        response.setIdLote(producto.getLote().getId());
        response.setNumeroSeguimientoLote(producto.getLote().getNumeroSeguimiento());
        response.setTipoProductoLote(producto.getLote().getTipoProducto());
        response.setActivo(producto.getActivo());
        response.setFechaCreacion(producto.getFechaCreacion().toString());
        response.setFechaActualizacion(producto.getFechaActualizacion().toString());

        // Convertir byte[] a base64 para el frontend
        if (producto.getFoto() != null && producto.getFoto().length > 0) {
            String fotoBase64 = Base64.getEncoder().encodeToString(producto.getFoto());
            response.setFotoBase64(fotoBase64);
        }

        return response;
    }

    // Métodos para manejo de imágenes
    public byte[] obtenerFotoProducto(Long idProducto) {
        Optional<Producto> productoOptional = productoRepository.findById(idProducto);
        return productoOptional.map(Producto::getFoto).orElse(null);
    }

    public CodigoResponse<Void> actualizarFotoProducto(Long idProducto, byte[] foto) {
        try {
            Optional<Producto> productoOptional = productoRepository.findById(idProducto);
            if (productoOptional.isEmpty()) {
                return new CodigoResponse<>(404, "Producto no encontrado", null);
            }

            Producto producto = productoOptional.get();
            producto.setFoto(foto);
            productoRepository.save(producto);

            return new CodigoResponse<>(200, "Foto actualizada correctamente", null);
        } catch (Exception e) {
            log.error("Error al actualizar foto: {}", e.getMessage(), e);
            return new CodigoResponse<>(500, "Error al actualizar foto", null);
        }
    }
}