package com.ApiarioSamano.MicroServiceNotificacionesGmail.exception;

public class DestinatarioInvalidoException extends EmailException {
    public DestinatarioInvalidoException(String destinatario) {
        super("El destinatario proporcionado es inválido: " + destinatario);
    }
}