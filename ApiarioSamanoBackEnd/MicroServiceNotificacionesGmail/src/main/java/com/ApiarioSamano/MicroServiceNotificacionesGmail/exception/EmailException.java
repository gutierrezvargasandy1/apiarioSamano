package com.ApiarioSamano.MicroServiceNotificacionesGmail.exception;

public class EmailException extends RuntimeException {
    public EmailException(String mensaje) {
        super(mensaje);
    }

    public EmailException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}