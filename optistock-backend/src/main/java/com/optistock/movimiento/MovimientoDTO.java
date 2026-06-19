package com.optistock.movimiento;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoDTO {

    private Integer idMovimiento;

    private LocalDateTime fecha;

    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;

    private String productoNombre;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor a cero")
    private BigDecimal costoUnitario;

    @NotBlank(message = "El tipo de movimiento es obligatorio (ENTRADA/SALIDA)")
    @Pattern(regexp = "^(ENTRADA|SALIDA)$", message = "El tipo de movimiento debe ser ENTRADA o SALIDA")
    private String tipoMovimiento;

    @NotBlank(message = "La referencia es obligatoria")
    private String referencia;

    @NotBlank(message = "El responsable es obligatorio")
    private String responsable;

    @Size(max = 255, message = "Las observaciones no pueden exceder los 255 caracteres")
    private String observaciones;

    public MovimientoDTO() {
    }

    // Getters y Setters
    public Integer getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Integer idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}