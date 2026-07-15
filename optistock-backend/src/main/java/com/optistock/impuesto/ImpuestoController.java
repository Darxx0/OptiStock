package com.optistock.impuesto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/impuestos")
public class ImpuestoController {

    private final ImpuestoService impuestoService;

    // Inyección limpia por constructor, idéntica a tu ProductoController
    public ImpuestoController(ImpuestoService impuestoService) {
        this.impuestoService = impuestoService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ImpuestoDTO>> getAll() {
        List<ImpuestoDTO> impuestos = impuestoService.findAll();
        if (impuestos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(impuestos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ImpuestoDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(impuestoService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR')") // O 'ROLE_ADMIN' estricto si solo el admin gestiona tasas
    public ResponseEntity<ImpuestoDTO> create(@RequestBody @Valid ImpuestoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(impuestoService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR')")
    public ResponseEntity<ImpuestoDTO> update(@PathVariable Integer id,
                                              @Valid @RequestBody ImpuestoDTO dto) {
        return ResponseEntity.ok(impuestoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        impuestoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}