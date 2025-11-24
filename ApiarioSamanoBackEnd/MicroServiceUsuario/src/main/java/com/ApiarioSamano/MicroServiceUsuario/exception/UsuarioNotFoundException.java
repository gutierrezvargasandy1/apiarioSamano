package com.ApiarioSamano.MicroServiceUsuario.exception;

// Usuario no encontrado
public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(Long id) {
        super("Usuario con ID " + id + " no encontrado.");
    }

    public UsuarioNotFoundException(String email) {
        super("Usuario con email " + email + " no encontrado.");
    }
}
