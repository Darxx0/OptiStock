package com.optistock.impuesto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ImpuestoDTO {

    private Integer id;

    @NotBlank(message = "El código del impuesto es obligatorio")
    @Size(min = 2, max = 10, message = "El código debe tener entre 2 y 10 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre del impuesto es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombre;

    @NotNull(message = "El porcentaje es obligatorio")
    @DecimalMin(value = "0.00", message = "El porcentaje no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El porcentaje no puede exceder el 100%")
    private BigDecimal porcentaje;

    private Boolean activo;

    public ImpuestoDTO() {
    }

    // Mapeador estático siguiendo tu mismo estándar de ProductoDTO
    public static ImpuestoDTO fromEntity(Impuesto i) {
        ImpuestoDTO dto = new ImpuestoDTO();
        dto.id = i.getIdImpuesto();
        dto.codigo = i.getCodigo();
        dto.nombre = i.getNombre();
        dto.porcentaje = i.getPorcentaje();
        dto.activo = i.getActivo();
        return dto;
    }

    // Getters y Setters...
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}