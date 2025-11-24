package com.ApiarioSamano.MicroServiceNotificacionesGmail.exception;

public class TemplateProcessingException extends EmailException {
    public TemplateProcessingException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}