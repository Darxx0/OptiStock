package com.optistock.producto;

import java.math.BigDecimal;

public class ProductoDTO {

    private Integer id;
    private String nombre;
    private BigDecimal precio;
    private String categoria;
    private String descripcion;
    private Integer idCategoria;
    private Integer cantidad;

    public ProductoDTO() {
    }

    // Convierte de entidad a DTO
    public static ProductoDTO fromEntity(Producto p) {
        ProductoDTO dto = new ProductoDTO();

        dto.id = p.getIdProducto();
        dto.nombre = p.getNombre();
        dto.precio = p.getPrecioUnitario();
        dto.descripcion = p.getDescripcion();
        dto.cantidad = p.getCantidad();

        if (p.getCategoria() != null) {
            dto.categoria = p.getCategoria().getNombre();
            dto.idCategoria = p.getCategoria().getIdCategoria();
        } else {
            dto.categoria = "";
            dto.idCategoria = null;
        }

        return dto;
    }

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}