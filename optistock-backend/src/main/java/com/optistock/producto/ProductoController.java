package com.optistock.producto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos") // 1. Ruta base estandarizada a v1
@CrossOrigin(origins = "${cors.allowed-origins}") // 2. CORS dinámico para permitir envío de tokens
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * GET /api/v1/productos
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductoDTO>> getAll() {
        List<ProductoDTO> productos = productoService.findAll();

        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content si el catálogo está vacío
        }

        return ResponseEntity.ok(productos);
    }

    /**
     * GET /api/v1/productos/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductoDTO> getById(@PathVariable Integer id) {
        // Asumiendo que tu productoService.findById ya maneja la excepción si no lo
        // encuentra
        return ResponseEntity.ok(productoService.findById(id));
    }

    /**
     * POST /api/v1/productos
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ProductoDTO> create(@RequestBody ProductoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.create(dto));
    }

    /**
     * PUT /api/v1/productos/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ProductoDTO> update(@PathVariable Integer id,
            @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(productoService.update(id, dto));
    }

    /**
     * DELETE /api/v1/productos/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
