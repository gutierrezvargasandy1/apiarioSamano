package com.example.MicroServiceGestorDeArchivos.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.MicroServiceGestorDeArchivos.dto.FileUploadResponse;
import com.example.MicroServiceGestorDeArchivos.entity.Archivo;
import com.example.MicroServiceGestorDeArchivos.repository.ArchivoRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.storage.local.directory}")
    private String storageDirectory;

    @Value("${app.storage.max-file-size}")
    private long maxFileSize;

    @Value("${app.server.url}")
    private String serverUrl;

    private final ArchivoRepository archivoRepository;
    private final MicroServiceClient microServiceClient;

    public FileStorageService(ArchivoRepository archivoRepository, MicroServiceClient microServiceClient) {
        this.archivoRepository = archivoRepository;
        this.microServiceClient = microServiceClient;
    }

    public FileUploadResponse guardarArchivo(MultipartFile archivo) throws IOException {
        try {
            log.info("Iniciando guardado de archivo: {}", archivo.getOriginalFilename());
            validarArchivo(archivo);

            String fileId = microServiceClient.generarIdArchivo();
            log.info("ID generado para archivo: {}", fileId);

            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreAlmacenado = fileId + "." + extension;
            String rutaCompleta = storageDirectory + File.separator + nombreAlmacenado;

            crearDirectorios();
            log.info("Guardando archivo físicamente en: {}", rutaCompleta);

            Path destino = Paths.get(rutaCompleta);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            Metadata metadata = extraerMetadata(archivo, rutaCompleta);
            log.info("Metadata extraída: esImagen={}, ancho={}, alto={}", metadata.esImagen, metadata.ancho,
                    metadata.alto);

            Archivo archivoEntity = new Archivo();
            archivoEntity.setId(fileId);
            archivoEntity.setNombreOriginal(archivo.getOriginalFilename());
            archivoEntity.setNombreAlmacenado(nombreAlmacenado);
            archivoEntity.setRutaCompleta(rutaCompleta);
            archivoEntity.setTipoMime(archivo.getContentType());
            archivoEntity.setTamaño(archivo.getSize());
            archivoEntity.setHash(calcularHash(archivo));
            archivoEntity.setFechaSubida(LocalDateTime.now());
            archivoEntity.setActivo(true);

            if (metadata.esImagen) {
                archivoEntity.setAncho(metadata.ancho);
                archivoEntity.setAlto(metadata.alto);
            }

            archivoRepository.save(archivoEntity);
            log.info("Archivo registrado en BD con ID: {}", fileId);

            return new FileUploadResponse(
                    fileId,
                    archivo.getOriginalFilename(),
                    serverUrl + "/api/files/download/" + fileId,
                    serverUrl + "/api/files/view/" + fileId,
                    archivo.getSize(),
                    archivo.getContentType(),
                    metadata.ancho,
                    metadata.alto,
                    LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error guardando archivo {}: {}", archivo.getOriginalFilename(), e.getMessage(), e);
            throw e;
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            log.error("Archivo vacío");
            throw new RuntimeException("El archivo está vacío");
        }
        if (archivo.getSize() > maxFileSize) {
            log.error("Archivo excede tamaño máximo: {} > {}", archivo.getSize(), maxFileSize);
            throw new RuntimeException("El archivo excede el tamaño máximo permitido");
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int i = nombreArchivo.lastIndexOf(".");
        String ext = (i != -1) ? nombreArchivo.substring(i + 1).toLowerCase() : "bin";
        log.info("Extensión del archivo {}: {}", nombreArchivo, ext);
        return ext;
    }

    private void crearDirectorios() throws IOException {
        Path directorio = Paths.get(storageDirectory);
        if (!Files.exists(directorio)) {
            log.info("Creando directorio de almacenamiento: {}", storageDirectory);
            Files.createDirectories(directorio);
        }
    }

    private Metadata extraerMetadata(MultipartFile archivo, String ruta) {
        Metadata metadata = new Metadata();
        if (archivo.getContentType() != null && archivo.getContentType().startsWith("image/")) {
            try {
                BufferedImage imagen = ImageIO.read(new File(ruta));
                if (imagen != null) {
                    metadata.esImagen = true;
                    metadata.ancho = imagen.getWidth();
                    metadata.alto = imagen.getHeight();
                    log.info("Metadata de imagen extraída: ancho={}, alto={}", metadata.ancho, metadata.alto);
                }
            } catch (IOException e) {
                log.warn("No se pudo extraer metadata de la imagen: {}", e.getMessage(), e);
            }
        }
        return metadata;
    }

    private String calcularHash(MultipartFile archivo) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(archivo.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            log.info("Hash calculado: {}", hexString);
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error al calcular hash del archivo: {}", e.getMessage(), e);
            throw new RuntimeException("Error al calcular hash del archivo", e);
        }
    }

    private static class Metadata {
        boolean esImagen = false;
        Integer ancho = null;
        Integer alto = null;
    }

    // Método para descargar un archivo por ID
    public ResponseEntity<Resource> descargarArchivo(String id) {
        try {
            Archivo archivo = archivoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Archivo no encontrado con ID: " + id));

            Path rutaArchivo = Paths.get(archivo.getRutaCompleta());
            if (!Files.exists(rutaArchivo)) {
                throw new RuntimeException("Archivo físico no encontrado en: " + archivo.getRutaCompleta());
            }

            Resource resource = new UrlResource(rutaArchivo.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(archivo.getTipoMime()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + archivo.getNombreOriginal() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error descargando archivo con ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("No se pudo descargar el archivo", e);
        }
    }

    // Método para eliminar un archivo físico y su registro en BD
    public void eliminarArchivo(String id) {
        Archivo archivo = archivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado con ID: " + id));

        try {
            Path rutaArchivo = Paths.get(archivo.getRutaCompleta());
            Files.deleteIfExists(rutaArchivo);
            archivoRepository.deleteById(id);
            log.info("Archivo eliminado física y lógicamente con ID: {}", id);
        } catch (IOException e) {
            log.error("Error eliminando archivo físico {}: {}", archivo.getRutaCompleta(), e.getMessage(), e);
            throw new RuntimeException("No se pudo eliminar el archivo físico", e);
        }
    }

    // Método para actualizar un archivo físico sin tocar la BD
    public void actualizarArchivoFisico(String id, MultipartFile nuevoArchivo) {
        Archivo archivo = archivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado con ID: " + id));

        try {
            Path rutaArchivo = Paths.get(archivo.getRutaCompleta());
            Files.copy(nuevoArchivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archivo físico actualizado correctamente en: {}", archivo.getRutaCompleta());
        } catch (IOException e) {
            log.error("Error actualizando archivo físico {}: {}", archivo.getRutaCompleta(), e.getMessage(), e);
            throw new RuntimeException("No se pudo actualizar el archivo físico", e);
        }
    }

    // Método para visualizar un archivo desde el front
    public ResponseEntity<Resource> verArchivo(String id) {
        try {
            Archivo archivo = archivoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Archivo no encontrado con ID: " + id));

            Path rutaArchivo = Paths.get(archivo.getRutaCompleta());
            if (!Files.exists(rutaArchivo)) {
                throw new RuntimeException("Archivo físico no encontrado en: " + archivo.getRutaCompleta());
            }

            Resource resource = new UrlResource(rutaArchivo.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(archivo.getTipoMime()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + archivo.getNombreOriginal() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error visualizando archivo con ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("No se pudo visualizar el archivo", e);
        }
    }

}
