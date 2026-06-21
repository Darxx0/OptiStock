package com.optistock.proveedor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/proveedores")
public class ProveedorController {

    private final ProveedorRepository repo;

    public ProveedorController(ProveedorRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /api/v1/proveedores
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProveedorDTO>> getAll() {
        List<ProveedorDTO> lista = repo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build(); // Devuelve 204 si aún no hay proveedores
        }

        return ResponseEntity.ok(lista);
    }

    /**
     * GET /api/v1/proveedores/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProveedorDTO> getById(@PathVariable Integer id) {
        Proveedor p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        return ResponseEntity.ok(toDTO(p));
    }

    /**
     * POST /api/v1/proveedores
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR')")
    public ResponseEntity<ProveedorDTO> create(@Valid @RequestBody ProveedorDTO dto) {
        // Validación básica de negocio
        if (dto.getRazonSocial() == null || dto.getRazonSocial().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La razón social es obligatoria");
        }

        Proveedor p = toEntity(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(repo.save(p)));
    }

    /**
     * PUT /api/v1/proveedores/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR')")
    public ResponseEntity<ProveedorDTO> update(@PathVariable Integer id, @Valid @RequestBody ProveedorDTO dto) {
        Proveedor p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));

        if (dto.getRazonSocial() == null || dto.getRazonSocial().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La razón social no puede estar vacía");
        }

        p.setRazonSocial(dto.getRazonSocial());
        p.setCorreo(dto.getCorreo());
        p.setTelefono(dto.getTelefono());
        p.setTipoCorreo(dto.getTipoCorreo());

        return ResponseEntity.ok(toDTO(repo.save(p)));
    }

    /**
     * DELETE /api/v1/proveedores/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado");
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ProveedorDTO toDTO(Proveedor p) {
        ProveedorDTO dto = new ProveedorDTO();
        dto.setIdProveedor(p.getIdProveedor());
        dto.setRazonSocial(p.getRazonSocial());
        dto.setCorreo(p.getCorreo());
        dto.setTelefono(p.getTelefono());
        dto.setTipoCorreo(p.getTipoCorreo());
        return dto;
    }

    private Proveedor toEntity(ProveedorDTO dto) {
        Proveedor p = new Proveedor();
        p.setRazonSocial(dto.getRazonSocial());
        p.setCorreo(dto.getCorreo());
        p.setTelefono(dto.getTelefono());
        p.setTipoCorreo(dto.getTipoCorreo());
        return p;
    }
}