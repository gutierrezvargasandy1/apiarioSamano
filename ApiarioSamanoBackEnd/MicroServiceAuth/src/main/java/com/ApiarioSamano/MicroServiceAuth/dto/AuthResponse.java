package com.ApiarioSamano.MicroServiceAuth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor

public class AuthResponse {
    private String token;

    public AuthResponse(String token) {
        this.token = token;

    }
}