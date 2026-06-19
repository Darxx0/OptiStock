package com.optistock.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * Filtro stateless que extrae y valida el JWT del header Authorization.
 * Si es válido, establece el Authentication en el SecurityContext.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = extractJwt(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Integer userId = tokenProvider.getUserIdFromJwt(jwt);
                String rol = tokenProvider.getRolFromJwt(jwt);

                // ─── BLINDAJE ANTI-DUPLICACIÓN DE PREFIJO ────────────────────────
                // Si el rol ya empieza con "ROLE_", lo dejamos quieto.
                // Si no lo tiene (ej: "VENDEDOR"), se lo agregamos para estandarizar.
                String authorityName = (rol != null && rol.startsWith("ROLE_")) ? rol : "ROLE_" + rol;
                // ─────────────────────────────────────────────────────────────────

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null,
                        Collections.singleton(new SimpleGrantedAuthority(authorityName)));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ex) {
            logger.error("Error al establecer autenticación JWT", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwt(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}