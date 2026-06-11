package com.optistock.pago;

import com.optistock.factura.Factura;
import com.optistock.factura.FacturaRepository;
import com.optistock.factura.FacturaDTO;
import com.optistock.factura.FacturaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
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
     * GET /api/pagos/pendientes
     * Devuelve las facturas que NO tienen ningún pago registrado.
     */
    @GetMapping("/pendientes")
    @Transactional(readOnly = true)
    public ResponseEntity<List<FacturaDTO>> getFacturasPendientes() {
        Set<Long> conPago = pagoRepo.findFacturasConPago();
        List<FacturaDTO> pendientes = facturaRepo.findAll().stream()
                .filter(f -> !conPago.contains(f.getIdFactura()))
                .map(facturaService::mapToDTOPublic)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pendientes);
    }

    /**
     * POST /api/pagos
     * Registra un pago para una factura (la marca como pagada).
     * Body: { idFactura, tipo, monto }
     */
    @PostMapping
    @Transactional
    public ResponseEntity<PagoDTO> registrarPago(@RequestBody PagoDTO dto) {
        Factura factura = facturaRepo.findById(dto.getIdFactura())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setTipo(dto.getTipo() != null ? dto.getTipo() : "Efectivo");
        pago.setMonto(dto.getMonto());
        pago.setFechaPago(LocalDateTime.now());

        Pago guardado = pagoRepo.save(pago);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(guardado));
    }

    /**
     * GET /api/pagos/factura/{id}
     * Lista los pagos de una factura específica.
     */
    @GetMapping("/factura/{id}")
    public ResponseEntity<List<PagoDTO>> getPagosByFactura(@PathVariable Long id) {
        return ResponseEntity.ok(
            pagoRepo.findByFacturaIdFactura(id).stream().map(this::toDTO).collect(Collectors.toList())
        );
    }

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
