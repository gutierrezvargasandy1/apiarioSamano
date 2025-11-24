package com.ApiarioSamano.MicroServiceAuth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequestDTO {
    private String email;
    private String otp;
    private String nuevaContrasena;
}
