package com.optistock.pago;

import com.optistock.factura.Factura;
import com.optistock.factura.FacturaRepository;
import com.optistock.factura.FacturaDTO;
import com.optistock.factura.FacturaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import com.optistock.audit.AuditoriaService;
import com.optistock.security.UsuarioActualService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pagos") // 1. Estandarización de la ruta base (v1)
public class PagoController {

    private static final Logger logger = LoggerFactory.getLogger(PagoController.class);

    private final PagoRepository pagoRepo;
    private final FacturaRepository facturaRepo;
    private final FacturaService facturaService;
    private final AuditoriaService auditoriaService;
    private final HttpServletRequest request;
    private final UsuarioActualService usuarioActualService;
    private final PagoService pagoService;

    public PagoController(PagoRepository pagoRepo, FacturaRepository facturaRepo, FacturaService facturaService,
            AuditoriaService auditoriaService, HttpServletRequest request, UsuarioActualService usuarioActualService,
            PagoService pagoService) {
        this.pagoRepo = pagoRepo;
        this.facturaRepo = facturaRepo;
        this.facturaService = facturaService;
        this.auditoriaService = auditoriaService;
        this.request = request;
        this.usuarioActualService = usuarioActualService;
        this.pagoService = pagoService;
    }

    /**
     * GET /api/v1/pagos/pendientes
     */
    @GetMapping("/pendientes")
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR', 'CONTADOR')")
    public ResponseEntity<List<FacturaDTO>> getFacturasPendientes() {
        Set<Long> conPago = pagoRepo.findFacturasConPago();
        List<FacturaDTO> pendientes = facturaRepo.findAll().stream()
                .filter(f -> !conPago.contains(f.getIdFactura()))
                .map(facturaService::mapToDTOPublic)
                .collect(Collectors.toList());

        if (pendientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(pendientes);
    }

    /**
     * POST /api/v1/pagos
     * Body: { idFactura, tipo, monto }
     */
    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR')")
    public ResponseEntity<PagoDTO> registrarPago(@Valid @RequestBody PagoDTO dto) {
        logger.info("Registrando pago. Factura: {}, Monto: {}", dto.getIdFactura(), dto.getMonto());
        
        // Uso del servicio para crear el pago
        Pago guardado = pagoService.crear(dto);

        PagoDTO responseDto = toDTO(guardado);
        logger.info("Pago registrado: ID={}", responseDto.getIdPago());
        
        Integer idUsuario = usuarioActualService.getIdUsuarioActual();
        auditoriaService.registrar(idUsuario, "CREATE", "pago", guardado.getIdPago(), "Registro de pago por monto " + guardado.getMonto(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * GET /api/v1/pagos/factura/{id}
     */
    @GetMapping("/factura/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VENDEDOR', 'CONTADOR')")
    public ResponseEntity<List<PagoDTO>> getPagosByFactura(@PathVariable Long id) {
        List<Pago> pagos = pagoRepo.findByFacturaIdFactura(id);

        if (pagos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                pagos.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private PagoDTO toDTO(Pago p) {
        PagoDTO dto = new PagoDTO();
        dto.setIdPago(p.getIdPago());
        dto.setIdFactura(p.getFactura().getIdFactura());
        dto.setTipo(p.getTipo());
        dto.setMonto(p.getMonto());
        dto.setFechaPago(p.getFechaPago());
        return dto;
    }
}
