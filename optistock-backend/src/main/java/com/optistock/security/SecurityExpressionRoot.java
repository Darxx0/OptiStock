package com.optistock.security;

import org.springframework.stereotype.Component;

@Component
public class SecurityExpressionRoot {

    private final UsuarioActualService usuarioActualService;

    public SecurityExpressionRoot(UsuarioActualService usuarioActualService) {
        this.usuarioActualService = usuarioActualService;
    }

    public boolean isCurrentUser(Integer userId) {
        return usuarioActualService.isCurrentUser(userId);
    }
}
