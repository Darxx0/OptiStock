package com.optistock.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:tu_clave_secreta_super_segura_de_minimo_32_caracteres}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 horas en ms
    private int jwtExpirationMs;

    // JJWT 0.12.x prefiere explícitamente SecretKey sobre Key genérico
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Integer userId, String nombreUsuario, String rol) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", nombreUsuario)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey()) // El algoritmo se deduce automáticamente de la clave
                .compact();
    }

    public Integer getUserIdFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload(); // .getBody() ahora es .getPayload()

        return Integer.parseInt(claims.getSubject());
    }

    public String getRolFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return (String) claims.get("rol");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}