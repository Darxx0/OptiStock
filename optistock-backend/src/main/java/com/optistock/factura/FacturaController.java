package com.optistock.factura;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/facturas")
@CrossOrigin(origins = "${cors.allowed-origins}") // Mantiene la compatibilidad con tus puertos de CORS
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    /**
     * POST /api/v1/facturas
     * Restricción: Solo usuarios con rol ADMIN o VENDEDOR pueden registrar una
     * venta/factura.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<FacturaDTO> crear(@RequestBody FacturaDTO dto) {
        FacturaDTO nuevaFactura = facturaService.crearFactura(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaFactura);
    }

    /**
     * GET /api/v1/facturas
     * Restricción: Cualquier usuario que haya iniciado sesión (autenticado) puede
     * listar las facturas.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FacturaDTO>> listar() {
        return ResponseEntity.ok(facturaService.listarFacturas());
    }

    /**
     * GET /api/v1/facturas/reportes/financieros
     * Restricción: Endpoint crítico. Solo accesibles para los roles ADMIN y
     * CONTADOR.
     */
    @GetMapping("/reportes/financieros")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<?> reporteFinanciero() {
        return ResponseEntity.ok("Servicio de reportes financieros activo (Próximamente)");
    }
}
