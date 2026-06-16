package com.optistock.categoria;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias") // 1. Ruta base unificada con versionado v1
@CrossOrigin(origins = "${cors.allowed-origins}") // 2. CORS dinámico compatible con credentials=true
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * GET /api/v1/categorias
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Categoria>> listarCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        if (categorias.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(categorias); // 200 OK
    }

    /**
     * GET /api/v1/categorias/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        return ResponseEntity.ok(categoria);
    }

    /**
     * POST /api/v1/categorias
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<Categoria> crearCategoria(@RequestBody Categoria categoria) {
        // Validación básica de negocio interna antes de guardar
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la categoría es obligatorio");
        }

        Categoria nuevaCategoria = categoriaRepository.save(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria); // 201 Created
    }

    /**
     * PUT /api/v1/categorias/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Integer id,
            @RequestBody Categoria datosActualizados) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        if (datosActualizados.getNombre() == null || datosActualizados.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la categoría no puede estar vacío");
        }

        categoriaExistente.setNombre(datosActualizados.getNombre());

        return ResponseEntity.ok(categoriaRepository.save(categoriaExistente));
    }

    /**
     * DELETE /api/v1/categorias/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}