package com.optistock.pago;

import com.optistock.factura.Factura;
import com.optistock.factura.FacturaRepository;
import com.optistock.factura.FacturaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final FacturaService facturaService;

    public PagoService(PagoRepository pagoRepository, FacturaRepository facturaRepository, FacturaService facturaService) {
        this.pagoRepository = pagoRepository;
        this.facturaRepository = facturaRepository;
        this.facturaService = facturaService;
    }

    public Pago crear(PagoDTO dto) {
        if (dto.getMonto() == null || dto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a cero");
        }

        Factura factura = facturaRepository.findById(dto.getIdFactura())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setMonto(dto.getMonto());
        pago.setTipo(dto.getTipo() != null ? dto.getTipo() : "EFECTIVO");
        pago.setFechaPago(LocalDateTime.now());

        return pagoRepository.save(pago);
    }
}
