package dto;

import jakarta.validation.constraints.NotBlank;

public class PreregistroVisitanteRequest {

    @NotBlank
    private String nombreCompleto;

    @NotBlank
    private String dniNie;

    @NotBlank
    private String password;

    private String nacionalidad;
    private String telefono;
    private String email;
    private String direccion;
    private String parentesco;
    private Boolean aceptaNormativa;

    public PreregistroVisitanteRequest() {
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDniNie() {
        return dniNie;
    }

    public void setDniNie(String dniNie) {
        this.dniNie = dniNie;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public Boolean getAceptaNormativa() {
        return aceptaNormativa;
    }

    public void setAceptaNormativa(Boolean aceptaNormativa) {
        this.aceptaNormativa = aceptaNormativa;
    }
}