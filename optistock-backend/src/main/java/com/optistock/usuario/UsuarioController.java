package com.optistock.usuario;

import com.optistock.rol.Rol;
import com.optistock.rol.RolRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;

    public UsuarioController(UsuarioRepository usuarioRepo, RolRepository rolRepo) {
        this.usuarioRepo = usuarioRepo;
        this.rolRepo = rolRepo;
    }

    /** GET /api/usuarios — lista todos (sin contraseñas) */
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAll() {
        return ResponseEntity.ok(
                usuarioRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /** GET /api/usuarios/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(toDTO(find(id)));
    }

    /**
     * POST /api/usuarios/login
     * Body: { usuarioLogin, contrasena }
     * Devuelve el DTO del usuario si las credenciales son correctas.
     * Punto de extensión: aquí irá la generación del JWT cuando se implemente.
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(@RequestBody Map<String, String> creds) {
        String login = creds.get("usuarioLogin");
        String pass = creds.get("contrasena");
        if (login == null || pass == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credenciales incompletas");

        Usuario u = usuarioRepo.findByUsuarioLoginIgnoreCase(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        // Comparación directa — reemplazar por BCrypt cuando se implemente seguridad
        if (!u.getContrasena().equals(pass))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña incorrecta");

        return ResponseEntity.ok(toDTO(u));
    }

    /** POST /api/usuarios */
    @PostMapping
    public ResponseEntity<UsuarioDTO> create(@RequestBody UsuarioDTO dto) {
        validarDTO(dto, null);
        if (usuarioRepo.existsByUsuarioLoginIgnoreCase(dto.getUsuarioLogin()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El login ya está en uso");

        Usuario u = toEntity(dto, new Usuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(usuarioRepo.save(u)));
    }

    /** PUT /api/usuarios/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> update(@PathVariable Integer id, @RequestBody UsuarioDTO dto) {
        validarDTO(dto, id);
        Usuario u = find(id);

        // Si el login cambió, verificar que no esté tomado
        if (!u.getUsuarioLogin().equalsIgnoreCase(dto.getUsuarioLogin()) &&
                usuarioRepo.existsByUsuarioLoginIgnoreCase(dto.getUsuarioLogin()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El login ya está en uso");

        toEntity(dto, u);
        // Solo actualizar contraseña si se envió una nueva
        if (dto.getContrasena() != null && !dto.getContrasena().isBlank())
            u.setContrasena(dto.getContrasena());

        return ResponseEntity.ok(toDTO(usuarioRepo.save(u)));
    }

    /** DELETE /api/usuarios/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        find(id);
        usuarioRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Usuario find(Integer id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void validarDTO(UsuarioDTO dto, Integer idExcluir) {
        if (dto.getNombre() == null || dto.getNombre().isBlank() ||
                dto.getApellido() == null || dto.getApellido().isBlank() ||
                dto.getUsuarioLogin() == null || dto.getUsuarioLogin().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre, apellido y login son obligatorios");
    }

    private UsuarioDTO toDTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(u.getIdUsuario());
        dto.setNombre(u.getNombre());
        dto.setApellido(u.getApellido());
        dto.setUsuarioLogin(u.getUsuarioLogin());
        // NUNCA incluir contrasena en la respuesta
        if (u.getRol() != null) {
            dto.setIdRol(u.getRol().getIdRol());
            dto.setNombreRol(u.getRol().getNombre());
        }
        return dto;
    }

    private Usuario toEntity(UsuarioDTO dto, Usuario u) {
        u.setNombre(dto.getNombre());
        u.setApellido(dto.getApellido());
        u.setUsuarioLogin(dto.getUsuarioLogin());
        if (dto.getContrasena() != null && !dto.getContrasena().isBlank())
            u.setContrasena(dto.getContrasena());
        if (dto.getIdRol() != null) {
            Rol rol = rolRepo.findById(dto.getIdRol())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado"));
            u.setRol(rol);
        }
        return u;
    }
}
