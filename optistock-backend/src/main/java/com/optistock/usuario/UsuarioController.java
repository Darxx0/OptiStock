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

import jakarta.servlet.http.HttpServletRequest;
import com.optistock.audit.AuditoriaService;
import com.optistock.security.UsuarioActualService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    private final HttpServletRequest request;
    private final UsuarioActualService usuarioActualService;

    public UsuarioController(UsuarioRepository usuarioRepo, RolRepository rolRepo,
            JwtTokenProvider tokenProvider, PasswordEncoder passwordEncoder,
            AuditoriaService auditoriaService, HttpServletRequest request,
            UsuarioActualService usuarioActualService) {
        this.usuarioRepo = usuarioRepo;
        this.rolRepo = rolRepo;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
        this.request = request;
        this.usuarioActualService = usuarioActualService;
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

        logger.info("Intento de login: {}", login);

        if (login == null || pass == null) {
            logger.warn("Login fallido: null. Razón: Credenciales incompletas");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credenciales incompletas");
        }

        Usuario u = usuarioRepo.findByUsuarioLoginIgnoreCase(login)
                .orElseThrow(() -> {
                    logger.warn("Login fallido: {}. Razón: Usuario no encontrado", login);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
                });

        if (!passwordEncoder.matches(pass, u.getContrasena())) {
            logger.warn("Login fallido: {}. Razón: Contraseña incorrecta", login);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }

        String token = tokenProvider.generateToken(
                u.getIdUsuario(),
                u.getUsuarioLogin(),
                u.getRol() != null ? u.getRol().getNombre() : "USER");

        logger.info("Login exitoso: {}", login);

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

        Usuario guardado = usuarioRepo.save(u);
        Integer idAdmin = usuarioActualService.getIdUsuarioActual();
        if(idAdmin == null) idAdmin = guardado.getIdUsuario();
        auditoriaService.registrar(idAdmin, "CREATE", "usuario", guardado.getIdUsuario(), "Registro de usuario", request);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(guardado));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UsuarioDTO> create(@Valid @RequestBody UsuarioDTO dto) {
        validarDTO(dto, null);

        if (usuarioRepo.existsByUsuarioLoginIgnoreCase(dto.getUsuarioLogin()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El login ya está en uso");

        Usuario u = toEntity(dto, new Usuario());
        u.setContrasena(passwordEncoder.encode(dto.getContrasena()));

        Usuario guardado = usuarioRepo.save(u);
        Integer idAdmin = usuarioActualService.getIdUsuarioActual();
        if(idAdmin == null) idAdmin = guardado.getIdUsuario();
        auditoriaService.registrar(idAdmin, "CREATE", "usuario", guardado.getIdUsuario(), "Creación de usuario por admin", request);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(guardado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @usuarioActualService.isCurrentUser(#id)")
    public ResponseEntity<UsuarioDTO> update(@PathVariable Integer id, @Valid @RequestBody UsuarioDTO dto) {
        validarDTO(dto, id);
        Usuario u = find(id);

        if (!u.getUsuarioLogin().equalsIgnoreCase(dto.getUsuarioLogin()) &&
                usuarioRepo.existsByUsuarioLoginIgnoreCase(dto.getUsuarioLogin()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El login ya está en uso");

        toEntity(dto, u);

        if (dto.getContrasena() != null && !dto.getContrasena().isBlank())
            u.setContrasena(passwordEncoder.encode(dto.getContrasena()));

        Usuario guardado = usuarioRepo.save(u);
        Integer idModificador = usuarioActualService.getIdUsuarioActual();
        if(idModificador == null) idModificador = guardado.getIdUsuario();
        auditoriaService.registrar(idModificador, "UPDATE", "usuario", guardado.getIdUsuario(), "Actualización de usuario", request);

        return ResponseEntity.ok(toDTO(guardado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        find(id);
        usuarioRepo.deleteById(id);
        
        Integer idModificador = usuarioActualService.getIdUsuarioActual();
        auditoriaService.registrar(idModificador, "DELETE", "usuario", id, "Eliminación de usuario", request);

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
