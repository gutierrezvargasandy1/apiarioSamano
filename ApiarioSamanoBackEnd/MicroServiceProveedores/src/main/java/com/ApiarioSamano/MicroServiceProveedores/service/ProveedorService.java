package com.ApiarioSamano.MicroServiceProveedores.service;

import com.ApiarioSamano.MicroServiceProveedores.dto.CodigoResponse;
import com.ApiarioSamano.MicroServiceProveedores.dto.ProveedorDTO.ProveedorRequest;
import com.ApiarioSamano.MicroServiceProveedores.model.Proveedor;
import com.ApiarioSamano.MicroServiceProveedores.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public CodigoResponse<List<Proveedor>> obtenerTodos() {
        List<Proveedor> proveedores = proveedorRepository.findAll();
        return new CodigoResponse<>(200, "Lista de proveedores obtenida correctamente", proveedores);
    }

    public CodigoResponse<Proveedor> obtenerPorId(Long id) {
        Optional<Proveedor> proveedor = proveedorRepository.findById(id);
        if (proveedor.isPresent()) {
            return new CodigoResponse<>(200, "Proveedor encontrado", proveedor.get());
        }
        return new CodigoResponse<>(404, "Proveedor no encontrado", null);
    }

    public CodigoResponse<Proveedor> guardarProveedor(ProveedorRequest request) {
        try {
            Proveedor proveedor = new Proveedor();

            // Convertir Base64 a byte[] si viene fotografía
            if (request.getFotografia() != null && !request.getFotografia().isEmpty()) {
                try {
                    // Limpiar el Base64 (remover prefijo data:image/... si existe)
                    String base64Puro = limpiarBase64(request.getFotografia());
                    byte[] fotografiaBytes = Base64.getDecoder().decode(base64Puro);
                    proveedor.setFotografia(fotografiaBytes);
                } catch (IllegalArgumentException e) {
                    // Si hay error en el Base64, guardar sin foto
                    System.err.println("Error al decodificar Base64: " + e.getMessage());
                    proveedor.setFotografia(null);
                }
            } else {
                proveedor.setFotografia(null);
            }

            proveedor.setNombreEmpresa(request.getNombreEmpresa());
            proveedor.setNombreRepresentante(request.getNombreReprecentante());
            proveedor.setNumTelefono(request.getNumTelefono());
            proveedor.setMaterialProvee(request.getMaterialProvee());

            Proveedor guardado = proveedorRepository.save(proveedor);
            return new CodigoResponse<>(201, "Proveedor registrado correctamente", guardado);

        } catch (Exception e) {
            System.err.println("Error al guardar proveedor: " + e.getMessage());
            e.printStackTrace();
            return new CodigoResponse<>(500, "Error interno al guardar el proveedor", null);
        }
    }

    public CodigoResponse<Proveedor> actualizarProveedor(Long id, ProveedorRequest request) {
        try {
            Optional<Proveedor> proveedorExistente = proveedorRepository.findById(id);
            if (proveedorExistente.isPresent()) {
                Proveedor proveedor = proveedorExistente.get();

                // Convertir Base64 a byte[] si viene fotografía
                if (request.getFotografia() != null && !request.getFotografia().isEmpty()) {
                    try {
                        // Limpiar el Base64 (remover prefijo data:image/... si existe)
                        String base64Puro = limpiarBase64(request.getFotografia());
                        byte[] fotografiaBytes = Base64.getDecoder().decode(base64Puro);
                        proveedor.setFotografia(fotografiaBytes);
                    } catch (IllegalArgumentException e) {
                        // Si hay error en el Base64, mantener la foto existente
                        System.err.println("Error al decodificar Base64: " + e.getMessage());
                        // No actualizamos la foto, mantenemos la existente
                    }
                } else {
                    // Si no viene fotografía, establecer como null
                    proveedor.setFotografia(null);
                }

                proveedor.setNombreEmpresa(request.getNombreEmpresa());
                proveedor.setNombreRepresentante(request.getNombreReprecentante());
                proveedor.setNumTelefono(request.getNumTelefono());
                proveedor.setMaterialProvee(request.getMaterialProvee());

                Proveedor actualizado = proveedorRepository.save(proveedor);
                return new CodigoResponse<>(200, "Proveedor actualizado correctamente", actualizado);
            }
            return new CodigoResponse<>(404, "Proveedor no encontrado", null);

        } catch (Exception e) {
            System.err.println("Error al actualizar proveedor: " + e.getMessage());
            e.printStackTrace();
            return new CodigoResponse<>(500, "Error interno al actualizar el proveedor", null);
        }
    }

    public CodigoResponse<Void> eliminarProveedor(Long id) {
        if (proveedorRepository.existsById(id)) {
            proveedorRepository.deleteById(id);
            return new CodigoResponse<>(200, "Proveedor eliminado correctamente", null);
        }
        return new CodigoResponse<>(404, "Proveedor no encontrado", null);
    }

    /**
     * Limpia el string Base64 removiendo el prefijo "data:image/...;base64," si
     * está presente
     */
    private String limpiarBase64(String base64ConPrefijo) {
        if (base64ConPrefijo == null)
            return null;

        // Si ya es Base64 puro, retornar tal cual
        if (!base64ConPrefijo.startsWith("data:")) {
            return base64ConPrefijo;
        }

        // Remover el prefijo "data:image/...;base64,"
        String[] partes = base64ConPrefijo.split(",");
        if (partes.length > 1) {
            return partes[1];
        }

        return base64ConPrefijo;
    }
}