package com.optistock.cliente;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.optistock.audit.AuditoriaService;
import com.optistock.security.UsuarioActualService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final AuditoriaService auditoriaService;
    private final HttpServletRequest request;
    private final UsuarioActualService usuarioActualService;

    public ClienteController(ClienteService clienteService, AuditoriaService auditoriaService, 
            HttpServletRequest request, UsuarioActualService usuarioActualService) {
        this.clienteService = clienteService;
        this.auditoriaService = auditoriaService;
        this.request = request;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClienteDTO>> getAll() {
        List<ClienteDTO> clientes = clienteService.obtenerTodos();
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClienteDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR')")
    public ResponseEntity<ClienteDTO> create(@Valid @RequestBody ClienteDTO dto) {
        ClienteDTO creado = clienteService.crear(dto);
        
        Integer idUsuario = usuarioActualService.getIdUsuarioActual();
        auditoriaService.registrar(idUsuario, "CREATE", "cliente", creado.getIdCliente().intValue(), "Creación de cliente", request);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR')")
    public ResponseEntity<ClienteDTO> update(@PathVariable Long id, @Valid @RequestBody ClienteDTO datos) {
        ClienteDTO actualizado = clienteService.actualizar(id, datos);

        Integer idUsuario = usuarioActualService.getIdUsuarioActual();
        auditoriaService.registrar(idUsuario, "UPDATE", "cliente", actualizado.getIdCliente().intValue(), "Actualización de cliente", request);

        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.eliminar(id);
        
        Integer idUsuario = usuarioActualService.getIdUsuarioActual();
        auditoriaService.registrar(idUsuario, "DELETE", "cliente", id.intValue(), "Eliminación de cliente", request);

        return ResponseEntity.noContent().build();
    }
}
