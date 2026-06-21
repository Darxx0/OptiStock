package com.optistock.movimiento;

import com.optistock.producto.Producto;
import com.optistock.producto.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/movimientos") // 1. Unificación de la ruta base al estándar v1
public class MovimientoController {

    private final MovimientoRepository movRepo;
    private final ProductoRepository productoRepo;

    public MovimientoController(MovimientoRepository movRepo, ProductoRepository productoRepo) {
        this.movRepo = movRepo;
        this.productoRepo = productoRepo;
    }

    /**
     * * GET /api/v1/movimientos
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MovimientoDTO>> getAll() {
        List<MovimientoDTO> lista = movRepo.findAllByOrderByFechaDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    /**
     * * GET /api/v1/movimientos/producto/{id}
     */
    @GetMapping("/producto/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MovimientoDTO>> getByProducto(@PathVariable Integer id) {
        return ResponseEntity.ok(
                movRepo.findByProductoIdProducto(id).stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /**
     * POST /api/v1/movimientos/ajuste
     */
    @PostMapping("/ajuste")
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Restricción estricta de seguridad
    public ResponseEntity<MovimientoDTO> registrarAjuste(@Valid @RequestBody MovimientoDTO dto) {
        Producto producto = productoRepo.findById(dto.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        int stockActual = producto.getCantidad() != null ? producto.getCantidad() : 0;
        boolean esEntrada = "Ajuste-Entrada".equalsIgnoreCase(dto.getTipoMovimiento());

        if (!esEntrada && dto.getCantidad() > stockActual) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente. Disponible: " + stockActual);
        }

        int nuevoStock = esEntrada ? stockActual + dto.getCantidad() : stockActual - dto.getCantidad();
        producto.setCantidad(nuevoStock);
        productoRepo.save(producto);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setFecha(LocalDateTime.now());
        mov.setProducto(producto);
        mov.setCantidad(dto.getCantidad());
        mov.setCostoUnitario(
                dto.getCostoUnitario() != null ? dto.getCostoUnitario() : producto.getPrecioUnitario());
        mov.setTipoMovimiento(dto.getTipoMovimiento());

        String ref = construirReferencia(dto.getReferencia(), dto.getResponsable(), dto.getObservaciones());
        mov.setReferencia(ref);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(movRepo.save(mov)));
    }

    /**
     * POST /api/v1/movimientos/transferencia
     */
    @PostMapping("/transferencia")
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR')")
    public ResponseEntity<MovimientoDTO> registrarTransferencia(@Valid @RequestBody MovimientoDTO dto) {
        Producto producto = productoRepo.findById(dto.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        int stockActual = producto.getCantidad() != null ? producto.getCantidad() : 0;
        if (dto.getCantidad() > stockActual) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente. Disponible: " + stockActual);
        }

        producto.setCantidad(stockActual - dto.getCantidad());
        productoRepo.save(producto);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setFecha(LocalDateTime.now());
        mov.setProducto(producto);
        mov.setCantidad(dto.getCantidad());
        mov.setCostoUnitario(
                dto.getCostoUnitario() != null ? dto.getCostoUnitario() : producto.getPrecioUnitario());
        mov.setTipoMovimiento("Transferencia");

        String ref = construirReferencia(dto.getReferencia(), dto.getResponsable(), dto.getObservaciones());
        mov.setReferencia(ref);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(movRepo.save(mov)));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String construirReferencia(String motivo, String responsable, String obs) {
        StringBuilder sb = new StringBuilder();
        if (motivo != null && !motivo.isBlank())
            sb.append(motivo);
        if (responsable != null && !responsable.isBlank())
            sb.append(" | ").append(responsable);
        if (obs != null && !obs.isBlank())
            sb.append(" | ").append(obs);
        String ref = sb.toString();
        return ref.length() > 100 ? ref.substring(0, 97) + "..." : ref;
    }

    private MovimientoDTO toDTO(MovimientoInventario m) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setIdMovimiento(m.getIdMovimiento());
        dto.setFecha(m.getFecha());
        dto.setProductoId(m.getProducto().getIdProducto());
        dto.setProductoNombre(m.getProducto().getNombre());
        dto.setCantidad(m.getCantidad());
        dto.setCostoUnitario(m.getCostoUnitario());
        dto.setTipoMovimiento(m.getTipoMovimiento());
        dto.setReferencia(m.getReferencia());
        return dto;
    }
}