package com.ivan.penitenciaria.api;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name = "incidentes")
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String tipo;          // Ej: pelea, fuga, robo

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "id_guardia", nullable = false)
    private Long idGuardia;

    @Column(name = "fecha_hora", nullable = false, length = 20)
    private String fechaHora;     // "2026-02-25 22:30"

    @Column(name = "id_reo")
    private Long idReo;           // Opcional: incidente asociado a un reo

    public Incidente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getIdGuardia() {
        return idGuardia;
    }

    public void setIdGuardia(Long idGuardia) {
        this.idGuardia = idGuardia;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Long getIdReo() {
        return idReo;
    }

    public void setIdReo(Long idReo) {
        this.idReo = idReo;
    }
}
