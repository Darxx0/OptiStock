package com.optistock.rol;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roles") // 1. Ruta base v1
@CrossOrigin(origins = "${cors.allowed-origins}") // 2. CORS dinámico
public class RolController {

    private final RolRepository repo;

    public RolController(RolRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /api/v1/roles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Rol>> getAll() {
        List<Rol> roles = repo.findAll();

        if (roles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(roles);
    }
}