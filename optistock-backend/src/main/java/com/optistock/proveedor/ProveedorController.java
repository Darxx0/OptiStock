package com.optistock.proveedor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    private final ProveedorRepository repo;

    public ProveedorController(ProveedorRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<ProveedorDTO>> getAll() {
        List<ProveedorDTO> lista = repo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDTO> getById(@PathVariable Integer id) {
        Proveedor p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        return ResponseEntity.ok(toDTO(p));
    }

    @PostMapping
    public ResponseEntity<ProveedorDTO> create(@RequestBody ProveedorDTO dto) {
        Proveedor p = toEntity(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(repo.save(p)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDTO> update(@PathVariable Integer id, @RequestBody ProveedorDTO dto) {
        Proveedor p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        p.setRazonSocial(dto.getRazonSocial());
        p.setCorreo(dto.getCorreo());
        p.setTelefono(dto.getTelefono());
        p.setTipoCorreo(dto.getTipoCorreo());
        return ResponseEntity.ok(toDTO(repo.save(p)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado");
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

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