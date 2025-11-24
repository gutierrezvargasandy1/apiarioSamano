package com.ApiarioSamano.MicroServiceUsuario.repository;

import com.ApiarioSamano.MicroServiceUsuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);

    // Verificar si existe un usuario por email
    boolean existsByEmail(String email);
}
