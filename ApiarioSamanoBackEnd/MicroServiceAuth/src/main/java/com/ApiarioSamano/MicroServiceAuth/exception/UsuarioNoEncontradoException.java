package com.ApiarioSamano.MicroServiceAuth.exception;

public class UsuarioNoEncontradoException extends AuthException {
    public UsuarioNoEncontradoException(String email) {
        super("Usuario no encontrado", 404,
                String.format("No se encontró un usuario con el email: %s", email));
    }
}