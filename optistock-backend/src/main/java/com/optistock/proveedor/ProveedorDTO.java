package com.optistock.proveedor;

import jakarta.validation.constraints.*;

public class ProveedorDTO {

    private Integer idProveedor;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(min = 3, max = 150, message = "La razón social debe tener entre 3 y 150 caracteres")
    private String razonSocial;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    private String correo;

    @Pattern(regexp = "^\\d{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos")
    private String telefono;

    @Pattern(regexp = "^(PERSONAL|CORPORATIVO)$", message = "El tipo de correo debe ser PERSONAL o CORPORATIVO")
    private String tipoCorreo;

    public ProveedorDTO() {
    }

    // Getters y Setters
    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTipoCorreo() {
        return tipoCorreo;
    }

    public void setTipoCorreo(String tipoCorreo) {
        this.tipoCorreo = tipoCorreo;
    }
}