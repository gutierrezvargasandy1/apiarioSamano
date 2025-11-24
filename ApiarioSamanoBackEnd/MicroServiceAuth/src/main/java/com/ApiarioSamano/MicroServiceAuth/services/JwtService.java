package com.ApiarioSamano.MicroServiceAuth.services;

import com.ApiarioSamano.MicroServiceAuth.exception.TokenInvalidoException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String rol, Long usuarioId, boolean estado,
            String nombre, String apellidoPaterno, String apellidoMaterno) {
        return Jwts.builder()
                .setSubject(email)
                .claim("rol", rol)
                .claim("usuarioId", usuarioId)
                .claim("estado", estado)
                .claim("nombre", nombre)
                .claim("apellidoPa", apellidoPaterno)
                .claim("apellidoMa", apellidoMaterno)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS384)
                .compact();
    }

    public String getEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new TokenInvalidoException();
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenInvalidoException();

        }
    }
}