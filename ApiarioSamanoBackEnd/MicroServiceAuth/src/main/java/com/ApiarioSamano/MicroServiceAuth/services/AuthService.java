package com.ApiarioSamano.MicroServiceAuth.services;

import com.ApiarioSamano.MicroServiceAuth.dto.AuthResponse;
import com.ApiarioSamano.MicroServiceAuth.dto.LoginRequest;
import com.ApiarioSamano.MicroServiceAuth.exception.CredencialesInvalidasException;
import com.ApiarioSamano.MicroServiceAuth.exception.UsuarioNoEncontradoException;
import com.ApiarioSamano.MicroServiceAuth.model.Usuario;
import com.ApiarioSamano.MicroServiceAuth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UsuarioRepository usuarioRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final MicroservicioClientService microservicioClientService;

        public AuthResponse login(LoginRequest request) {
                Usuario usuario = usuarioRepository.findByemail(request.getEmail())
                                .orElseThrow(() -> new UsuarioNoEncontradoException(request.getEmail()));

                if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
                        throw new CredencialesInvalidasException();
                }

                String token = jwtService.generateToken(
                                usuario.getEmail(),
                                usuario.getRol(),
                                usuario.getId(),
                                usuario.isEstado(),
                                usuario.getNombre(),
                                usuario.getApellidoMa(),
                                usuario.getApellidoPa());

                return AuthResponse.builder()
                                .token(token)
                                .build();
        }

        public void iniciarRecuperacion(String email) {
                Usuario usuario = usuarioRepository.findByemail(email)
                                .orElseThrow(() -> new UsuarioNoEncontradoException(email));

                // Generar OTP usando microservicio
                String otp = microservicioClientService.generarOtp();

                // Guardar OTP temporalmente en el usuario
                usuario.setOtp(otp);
                usuario.setOtpExpiracion(LocalDateTime.now().plusMinutes(5));
                usuarioRepository.save(usuario);

                // Preparar variables para el correo
                Map<String, Object> variables = Map.of(
                                "nombreUsuario", usuario.getNombre(),
                                "codigoVerificacion", otp,
                                "fecha", java.time.LocalDate.now().toString());

                // Enviar OTP por correo usando microservicio
                microservicioClientService.enviarCorreo(
                                email,
                                "Código OTP para recuperación de contraseña",
                                variables);
        }

        public boolean verificarOtpYCambiarContrasena(String email, String otp) {
                Usuario usuario = usuarioRepository.findByemail(email)
                                .orElseThrow(() -> new UsuarioNoEncontradoException(email));

                if (usuario.getOtpExpiracion() == null || usuario.getOtpExpiracion().isBefore(LocalDateTime.now())) {
                        usuario.setOtp(null);
                        usuario.setOtpExpiracion(null);
                        usuarioRepository.save(usuario);
                        throw new CredencialesInvalidasException(); // OTP expirado
                }

                if (!otp.equals(usuario.getOtp())) {
                        throw new CredencialesInvalidasException();
                }

                return true;
        }

        public void cambiarContrasena(String email, String nuevaContrasena, String otp) {
                Usuario usuario = usuarioRepository.findByemail(email)
                                .orElseThrow(() -> new UsuarioNoEncontradoException(email));

                if (!verificarOtpYCambiarContrasena(email, otp)) {
                        throw new CredencialesInvalidasException();
                }
                usuario.setOtp(null);
                usuario.setOtpExpiracion(null);
                usuario.setEstado(false);
                usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
                usuarioRepository.save(usuario);

        }

        public void cambiarContrasenaTemporal(String email, String contrasenaTemporal, String nuevaContrasena) {
                Usuario usuario = usuarioRepository.findByemail(email)
                                .orElseThrow(() -> new UsuarioNoEncontradoException(email));

                if (!usuario.isEstado()) {
                        throw new CredencialesInvalidasException();
                }
                // Validar la contraseña temporal
                if (!passwordEncoder.matches(contrasenaTemporal, usuario.getContrasena())) {
                        throw new CredencialesInvalidasException();
                }

                // Actualizar a la nueva contraseña
                usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
                usuario.setEstado(false);
                usuarioRepository.save(usuario);
        }

}