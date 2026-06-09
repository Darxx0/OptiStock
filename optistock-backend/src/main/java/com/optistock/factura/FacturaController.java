package com.optistock.factura;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "*")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    public List<FacturaDTO> listarFacturas() {
        return facturaService.listarFacturas();
    }

    @GetMapping("/{id}")
    public FacturaDTO obtenerFactura(@PathVariable Long id) {
        return facturaService.obtenerFactura(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacturaDTO crearFactura(@RequestBody FacturaDTO dto) {
        return facturaService.crearFactura(dto);
    }
}
