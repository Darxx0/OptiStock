package com.optistock.usuario;

import com.optistock.rol.Rol;
import com.optistock.rol.RolRepository;
import com.optistock.security.JwtTokenProvider;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/usuarios")
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

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> getAll() {
        return ResponseEntity.ok(
                usuarioRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @usuarioActualService.isCurrentUser(#id)")
    public ResponseEntity<UsuarioDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(toDTO(find(id)));
    }

    /** Endpoint público — autenticación con JWT */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String login = creds.get("usuarioLogin");
        String pass = creds.get("contrasena");

        if (login == null || pass == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credenciales incompletas");

        Usuario u = usuarioRepo.findByUsuarioLoginIgnoreCase(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(pass, u.getContrasena()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Usuario o contraseña incorrectos");

        String token = tokenProvider.generateToken(
                u.getIdUsuario(),
                u.getUsuarioLogin(),
                u.getRol() != null ? u.getRol().getNombre() : "USER");

        return ResponseEntity.ok(Map.of(
                "token", token,
                "usuario", toDTO(u)));
    }

    /** Endpoint público — registro de nuevos usuarios */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registro(@Valid @RequestBody UsuarioDTO dto) {
        validarDTO(dto, null);

        if (usuarioRepo.existsByUsuarioLoginIgnoreCase(dto.getUsuarioLogin()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El login ya está en uso");

        Usuario u = toEntity(dto, new Usuario());
        u.setContrasena(passwordEncoder.encode(dto.getContrasena()));

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(usuarioRepo.save(u)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UsuarioDTO> create(@RequestBody UsuarioDTO dto) {
        validarDTO(dto, null);

        if (usuarioRepo.existsByUsuarioLoginIgnoreCase(dto.getUsuarioLogin()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El login ya está en uso");

        Usuario u = toEntity(dto, new Usuario());
        u.setContrasena(passwordEncoder.encode(dto.getContrasena()));

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(usuarioRepo.save(u)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @usuarioActualService.isCurrentUser(#id)")
    public ResponseEntity<UsuarioDTO> update(@PathVariable Integer id, @RequestBody UsuarioDTO dto) {
        validarDTO(dto, id);
        Usuario u = find(id);

        if (!u.getUsuarioLogin().equalsIgnoreCase(dto.getUsuarioLogin()) &&
                usuarioRepo.existsByUsuarioLoginIgnoreCase(dto.getUsuarioLogin()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El login ya está en uso");

        toEntity(dto, u);

        if (dto.getContrasena() != null && !dto.getContrasena().isBlank())
            u.setContrasena(passwordEncoder.encode(dto.getContrasena()));

        return ResponseEntity.ok(toDTO(usuarioRepo.save(u)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        find(id);
        usuarioRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── helpers ──────────────────────────────────────────────────────

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
