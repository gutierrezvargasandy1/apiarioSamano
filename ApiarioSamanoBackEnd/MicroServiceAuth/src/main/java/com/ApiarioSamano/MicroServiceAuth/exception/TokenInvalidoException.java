package com.ApiarioSamano.MicroServiceAuth.exception;

public class TokenInvalidoException extends AuthException {
    public TokenInvalidoException() {
        super("Token inválido", 401, "El token JWT proporcionado no es válido o ha expirado");
    }
}