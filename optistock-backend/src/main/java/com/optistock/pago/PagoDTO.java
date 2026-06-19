package com.optistock.pago;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoDTO {

    private Integer idPago;

    @NotNull(message = "El ID de la factura es obligatorio")
    private Long idFactura;

    @NotBlank(message = "El tipo de pago es obligatorio")
    @Pattern(regexp = "^(EFECTIVO|TARJETA|TRANSFERENCIA|NEQUI|DAVIPLATA)$", message = "El tipo de pago no es válido")
    private String tipo;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    private LocalDateTime fechaPago;

    public PagoDTO() {
    }

    // Getters y Setters
    public Integer getIdPago() {
        return idPago;
    }

    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }

    public Long getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Long idFactura) {
        this.idFactura = idFactura;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}
