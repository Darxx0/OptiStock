package com.optistock.factura;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FacturaDTO {

    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String cliente;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 5, max = 20, message = "El documento debe ser válido")
    private String documento;

    @Email(message = "El formato del email es inválido")
    private String email;

    @Pattern(regexp = "^\\d{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos")
    private String telefono;

    private LocalDateTime fecha;

    @NotEmpty(message = "La factura debe incluir al menos un producto")
    @Valid
    private List<FacturaItemDTO> items;

    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;

    public FacturaDTO() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<FacturaItemDTO> getItems() {
        return items;
    }

    public void setItems(List<FacturaItemDTO> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}