package model;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;  // JSON

@XmlRootElement
@Entity
@Table(name = "reos")
public class Reo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(length = 200)
    private String delito;

    // ✅ VACÍO PÚBLICO OBLIGATORIO JPA/JSON
    public Reo() {}

    public Reo(String nombre, String dni, String delito) {
        this.nombre = nombre;
        this.dni = dni;
        this.delito = delito;
    }

    // Getters/Setters todos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getDelito() { return delito; }
    public void setDelito(String delito) { this.delito = delito; }
}
