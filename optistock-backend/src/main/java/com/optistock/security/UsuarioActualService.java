package com.optistock.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioActualService {

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     * En arquitecturas JWT, el ID suele guardarse como el 'subject' (String)
     * dentro del Principal de Spring Security.
     */
    public Integer getIdUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            try {
                // Si guardaste el ID directamente como String en el subject dentro del filtro
                return Integer.parseInt(auth.getPrincipal().toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException("El principal de autenticación no contiene un ID numérico válido");
            }
        }
        throw new RuntimeException("No hay usuario autenticado en el contexto de seguridad");
    }

    /**
     * Obtiene el rol (o roles separados por coma) del usuario autenticado de forma
     * limpia
     */
    public String getRolUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            StringBuilder roles = new StringBuilder();
            for (GrantedAuthority authority : auth.getAuthorities()) {
                if (roles.length() > 0)
                    roles.append(",");
                // Removemos el prefijo ROLE_ de Spring si existe para mantener tu estándar
                roles.append(authority.getAuthority().replace("ROLE_", ""));
            }
            return roles.toString();
        }
        return null;
    }

    /**
     * Verifica si el usuario actual es el propietario del recurso
     */
    public boolean isCurrentUser(Integer userId) {
        if (userId == null)
            return false;
        try {
            return getIdUsuarioActual().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica de forma estricta y segura si el usuario tiene un rol específico
     */
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null || role == null) {
            return false;
        }

        // Comparamos de forma exacta elemento por elemento en lugar de usar .contains()
        // en un String burdo
        String busquedaConPrefijo = "ROLE_" + role;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(role)
                        || a.getAuthority().equalsIgnoreCase(busquedaConPrefijo));
    }
}
