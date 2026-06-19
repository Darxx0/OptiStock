package com.optistock.producto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductoDTO {

    private Integer id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal precio;

    private String categoria;

    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    private String descripcion;

    @NotNull(message = "El ID de la categoría es obligatorio")
    private Integer idCategoria;

    @NotNull(message = "La cantidad inicial es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidad;

    public ProductoDTO() {
    }

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

    // Getters y Setters...
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