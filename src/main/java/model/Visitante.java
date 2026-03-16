package model;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@Entity
@Table(name = "visitantes")
public class Visitante implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellidos;
    @Column(unique = true, nullable = false)
    private String dni;
    private String email;
    @Column(name = "estado", nullable = false)
    private String estado = "PENDIENTE";
    @Lob
    @Column(name = "foto_documento", columnDefinition = "TEXT")
    private String fotoDocumento;

    // Constructor vacío
    public Visitante() {}

    // Getters y Setters COMPLETOS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFotoDocumento() { return fotoDocumento; }
    public void setFotoDocumento(String fotoDocumento) { this.fotoDocumento = fotoDocumento; }
}
