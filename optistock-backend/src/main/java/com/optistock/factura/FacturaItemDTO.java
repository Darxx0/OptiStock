package com.optistock.factura;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class FacturaItemDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser un número positivo")
    private Integer productoId;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal precio;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    private Integer cantidad;

    public FacturaItemDTO() {
    }

    // Getters y Setters
    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}