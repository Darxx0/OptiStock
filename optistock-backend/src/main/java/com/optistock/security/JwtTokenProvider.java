package com.optistock.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Proveedor JWT stateless — genera y valida tokens usando JJWT 0.12.x.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private int jwtExpirationMs;

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
                .signWith(getSigningKey())
                .compact();
    }

    public Integer getUserIdFromJwt(String token) {
        return Integer.parseInt(parseClaims(token).getSubject());
    }

    public String getRolFromJwt(String token) {
        return (String) parseClaims(token).get("rol");
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Parseo centralizado — evita repetir la cadena parser/verify/parse. */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}