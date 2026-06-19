package com.optistock.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Servicio para consultar datos del usuario autenticado en el SecurityContext.
 * Usado directamente en SpEL: @usuarioActualService.isCurrentUser(#id)
 */
@Service
public class UsuarioActualService {

    public Integer getIdUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            try {
                return Integer.parseInt(auth.getPrincipal().toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Principal no contiene ID numérico válido");
            }
        }
        throw new RuntimeException("No hay usuario autenticado");
    }

    public String getRolUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            return null;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .collect(Collectors.joining(","));
    }

    /** Verifica si el usuario autenticado es el propietario del recurso. */
    public boolean isCurrentUser(Integer userId) {
        if (userId == null) return false;
        try {
            return getIdUsuarioActual().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null || role == null) {
            return false;
        }
        String conPrefijo = "ROLE_" + role;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(role)
                        || a.getAuthority().equalsIgnoreCase(conPrefijo));
    }
}
