package com.ApiarioSamano.MicroServiceUsuario.services;

import com.ApiarioSamano.MicroServiceUsuario.exception.UsuarioAlreadyExistsException;
import com.ApiarioSamano.MicroServiceUsuario.exception.UsuarioNotFoundException;
import com.ApiarioSamano.MicroServiceUsuario.model.Usuario;
import com.ApiarioSamano.MicroServiceUsuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final MicroservicioClientService microservicioClientService;
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    public UsuarioService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            MicroservicioClientService microservicioClientService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.microservicioClientService = microservicioClientService;
        log.info("MicroservicioClientService inyectado: {}", this.microservicioClientService != null);

    }

    // Crear usuario con contraseña temporal y enviar correo
    public Usuario guardarUsuario(Usuario usuario, String jwt) {
        log.info("JWT recibido en guardarUsuario: {}", jwt);
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        if (usuario.getEmail() != null && usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new UsuarioAlreadyExistsException(usuario.getEmail());
        }

        log.info("Solicitando contraseña temporal al microservicio de generación...");
        String contrasenaTemporal = microservicioClientService.generarContrasena(jwt);
        log.info("Contraseña temporal recibida: {}", contrasenaTemporal);
        String contrasenaEncriptada = passwordEncoder.encode(contrasenaTemporal);
        usuario.setContrasena(contrasenaEncriptada);
        usuario.setEstado(true);

        // Guardar usuario
        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        log.info("Usuario {} creado con contraseña temporal.", usuario.getEmail());

        // Preparar variables para la plantilla Thymeleaf
        Map<String, Object> variables = Map.of(
                "nombreUsuario", usuario.getNombre(),
                "contrasenaTemporal", contrasenaTemporal);

        log.info("Enviando contraseña temporal al correo {}", usuario.getEmail());
        microservicioClientService.enviarCorreo(
                usuario.getEmail(),
                "Tu contraseña temporal",
                variables,
                jwt);

        return nuevoUsuario;
    }

    // Obtener todos los usuarios
    public List<Usuario> obtenerUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No hay usuarios registrados en el sistema.");
        }
        return usuarios;
    }

    // Obtener usuario por ID
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    // Obtener usuario por Email
    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));
    }

    // Eliminar usuario
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNotFoundException(id);
        }
        usuarioRepository.deleteById(id);
        log.info("Usuario con ID {} eliminado.", id);
    }

    // Validar existencia por email
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    // Actualizar usuario por email
    public Usuario actualizarPorEmail(String email, Usuario usuarioActualizado) {
        Usuario usuarioExistente = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));

        // Actualizamos solo los campos que vienen no nulos
        if (usuarioActualizado.getNombre() != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }
        if (usuarioActualizado.getApellidoPa() != null) {
            usuarioExistente.setApellidoPa(usuarioActualizado.getApellidoPa());
        }
        if (usuarioActualizado.getApellidoMa() != null) {
            usuarioExistente.setApellidoMa(usuarioActualizado.getApellidoMa());
        }
        if (usuarioActualizado.getContrasena() != null) {
            usuarioExistente.setContrasena(usuarioActualizado.getContrasena());
        }
        if (usuarioActualizado.getRol() != null) {
            usuarioExistente.setRol(usuarioActualizado.getRol());
        }
        if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().equals(email)) {
            // Validar que el nuevo email no exista
            if (usuarioRepository.existsByEmail(usuarioActualizado.getEmail())) {
                throw new UsuarioAlreadyExistsException(usuarioActualizado.getEmail());
            }
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
        }

        Usuario actualizado = usuarioRepository.save(usuarioExistente);
        log.info("Usuario con email {} actualizado.", email);
        return actualizado;
    }
}
