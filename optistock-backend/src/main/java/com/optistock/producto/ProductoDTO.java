package com.optistock.producto;

import java.math.BigDecimal;

/**
 * DTO que expone los campos que el frontend (datos.js) espera:
 *   id, nombre, precio, cantidad, categoria (String), descripcion
 *
 * La tabla producto no tiene columna cantidad; se retorna 0 por defecto.
 * El frontend puede seguir leyendo y escribiendo con la misma forma.
 */
public class ProductoDTO {

    private Integer id;
    private String nombre;
    private BigDecimal precio;
    private int cantidad;
    private String categoria;
    private String descripcion;
    private Integer idCategoria;

    public ProductoDTO() {}

    // Constructor desde entidad
    public static ProductoDTO fromEntity(Producto p) {
        ProductoDTO dto = new ProductoDTO();
        dto.id = p.getIdProducto();
        dto.nombre = p.getNombre();
        dto.precio = p.getPrecioUnitario();
        dto.cantidad = 0; // campo no presente en tabla producto
        dto.categoria = p.getCategoria() != null ? p.getCategoria().getNombre() : "";
        dto.idCategoria = p.getCategoria() != null ? p.getCategoria().getIdCategoria() : null;
        dto.descripcion = p.getDescripcion();
        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }
}
