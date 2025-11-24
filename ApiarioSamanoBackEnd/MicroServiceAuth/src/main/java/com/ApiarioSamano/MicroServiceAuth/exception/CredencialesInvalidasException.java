package com.ApiarioSamano.MicroServiceAuth.exception;

public class CredencialesInvalidasException extends AuthException {
    public CredencialesInvalidasException() {
        super("Credenciales inválidas", 401, "El email o contraseña son incorrectos");
    }
}