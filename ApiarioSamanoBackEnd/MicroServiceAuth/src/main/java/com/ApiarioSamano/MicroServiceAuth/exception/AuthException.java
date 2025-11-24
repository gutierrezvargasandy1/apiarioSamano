package com.ApiarioSamano.MicroServiceAuth.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final int statusCode;
    private final String description;

    public AuthException(String message, int statusCode, String description) {
        super(message);
        this.statusCode = statusCode;
        this.description = description;
    }
}