package com.optistock.factura;

import java.math.BigDecimal;

public class FacturaItemDTO {
    private Integer productoId;
    private String nombre;
    private BigDecimal precio;
    private Integer cantidad;

    public FacturaItemDTO() {}

    public Integer getProductoId() { return productoId; }
    public void setProductoId(Integer productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
