package com.optistock.cliente;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes") // 1. Unificación al estándar de versión v1
public class ClienteController {

    private final ClienteRepository clienteRepository;

    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * GET /api/v1/clientes
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Cliente>> getAll() {
        List<Cliente> clientes = clienteRepository.findAll();
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    /**
     * GET /api/v1/clientes/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Cliente> getById(@PathVariable Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        return ResponseEntity.ok(cliente);
    }

    /**
     * POST /api/v1/clientes
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR')")
    public ResponseEntity<Cliente> create(@RequestBody Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().isBlank() ||
                cliente.getNumeroDocumento() == null || cliente.getNumeroDocumento().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre y el número de documento son obligatorios");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteRepository.save(cliente));
    }

    /**
     * PUT /api/v1/clientes/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR')")
    public ResponseEntity<Cliente> update(@PathVariable Long id, @RequestBody Cliente datos) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        if (datos.getNombre() == null || datos.getNombre().isBlank() ||
                datos.getNumeroDocumento() == null || datos.getNumeroDocumento().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los campos obligatorios no pueden estar vacíos");
        }

        clienteExistente.setNombre(datos.getNombre());
        clienteExistente.setApellido(datos.getApellido());
        clienteExistente.setTipoDocumento(datos.getTipoDocumento());
        clienteExistente.setNumeroDocumento(datos.getNumeroDocumento());

        return ResponseEntity.ok(clienteRepository.save(clienteExistente));
    }

    /**
     * DELETE /api/v1/clientes/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }
        clienteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
