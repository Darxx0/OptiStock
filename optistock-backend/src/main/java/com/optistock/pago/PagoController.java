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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pagos") // 1. Estandarización de la ruta base (v1)
@CrossOrigin(origins = "${cors.allowed-origins}") // 2. Protección de CORS para el entorno
public class PagoController {

    private final PagoRepository pagoRepo;
    private final FacturaRepository facturaRepo;
    private final FacturaService facturaService;

    public PagoController(PagoRepository pagoRepo,
            FacturaRepository facturaRepo,
            FacturaService facturaService) {
        this.pagoRepo = pagoRepo;
        this.facturaRepo = facturaRepo;
        this.facturaService = facturaService;
    }

    /**
     * GET /api/v1/pagos/pendientes
     */
    @GetMapping("/pendientes")
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CONTADOR')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<PagoDTO> registrarPago(@RequestBody PagoDTO dto) {
        Factura factura = facturaRepo.findById(dto.getIdFactura())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        // Validación de negocio adicional: Evitar montos negativos o en cero
        if (dto.getMonto() == null || dto.getMonto().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto del pago debe ser mayor a cero");
        }

        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setTipo(dto.getTipo() != null && !dto.getTipo().isBlank() ? dto.getTipo() : "Efectivo");
        pago.setMonto(dto.getMonto());
        pago.setFechaPago(LocalDateTime.now());

        Pago guardado = pagoRepo.save(pago);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(guardado));
    }

    /**
     * GET /api/v1/pagos/factura/{id}
     */
    @GetMapping("/factura/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'CONTADOR')")
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
