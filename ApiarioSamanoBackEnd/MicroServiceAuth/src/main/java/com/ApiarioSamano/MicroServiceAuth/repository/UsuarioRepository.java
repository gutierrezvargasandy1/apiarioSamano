package com.ApiarioSamano.MicroServiceAuth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ApiarioSamano.MicroServiceAuth.model.Usuario;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por email
    Optional<Usuario> findByemail(String email);

    // Verificar si existe un usuario por email
    boolean existsByemail(String email);
}
