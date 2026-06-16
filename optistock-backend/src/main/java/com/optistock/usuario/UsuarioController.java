package com.optistock.usuario;

import com.optistock.rol.Rol;
import com.optistock.rol.RolRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.optistock.security.JwtTokenProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/usuarios")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class UsuarioController {

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepo, RolRepository rolRepo,
            JwtTokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.rolRepo = rolRepo;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /** GET /api/v1/usuarios — Solo ADMIN puede ver la lista global */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> getAll() {
        return ResponseEntity.ok(
                usuarioRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /** GET /api/v1/usuarios/{id} — ADMIN o el propio usuario autenticado */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isCurrentUser(#id)") // Expresión limpia enlazada a tu Root
    public ResponseEntity<UsuarioDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(toDTO(find(id)));
    }

    /**
     * POST /api/v1/usuarios/login
     * Endpoint público para autenticación
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String login = creds.get("usuarioLogin");
        String pass = creds.get("contrasena");

        if (login == null || pass == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credenciales incompletas");

        Usuario u = usuarioRepo.findByUsuarioLoginIgnoreCase(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuario o contraseña incorrectos"));

        // Validar contraseña hasheada
        if (!passwordEncoder.matches(pass, u.getContrasena()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Usuario o contraseña incorrectos");

        // Generar JWT usando el provider adaptado a JJWT 0.12.x
        String token = tokenProvider.generateToken(
                u.getIdUsuario(),
                u.getUsuarioLogin(),
                u.getRol() != null ? u.getRol().getNombre() : "USER");

        return ResponseEntity.ok(Map.of(
                "token", token,
                "usuario", toDTO(u)));
    }

    /**
     * POST /api/v1/usuarios
     * Solo ADMIN puede crear usuarios de forma manual
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> create(@RequestBody UsuarioDTO dto) {
        validarDTO(dto, null);

        if (usuarioRepo.existsByUsuarioLoginIgnoreCase(dto.getUsuarioLogin()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El login ya está en uso");

        Usuario u = toEntity(dto, new Usuario());

        // Hash de contraseña inicial
        u.setContrasena(passwordEncoder.encode(dto.getContrasena()));

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(usuarioRepo.save(u)));
    }

    /** PUT /api/v1/usuarios/{id} — ADMIN o el propio usuario */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isCurrentUser(#id)") // Expresión limpia enlazada a tu Root
    public ResponseEntity<UsuarioDTO> update(@PathVariable Integer id, @RequestBody UsuarioDTO dto) {
        validarDTO(dto, id);
        Usuario u = find(id);

        // Si el login cambió, verificar que no esté tomado
        if (!u.getUsuarioLogin().equalsIgnoreCase(dto.getUsuarioLogin()) &&
                usuarioRepo.existsByUsuarioLoginIgnoreCase(dto.getUsuarioLogin()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El login ya está en uso");

        toEntity(dto, u);

        // CORRECCIÓN: Si modificó la contraseña, se hashea de forma segura antes de
        // persistir
        if (dto.getContrasena() != null && !dto.getContrasena().isBlank())
            u.setContrasena(passwordEncoder.encode(dto.getContrasena()));

        return ResponseEntity.ok(toDTO(usuarioRepo.save(u)));
    }

    /** DELETE /api/v1/usuarios/{id} — Solo ADMIN puede borrar */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
        if (dto.getIdRol() != null) {
            Rol rol = rolRepo.findById(dto.getIdRol())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado"));
            u.setRol(rol);
        }
        return u;
    }
}
