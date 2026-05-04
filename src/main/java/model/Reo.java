package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "reos")
public class Reo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reos_seq")
    @SequenceGenerator(name = "reos_seq", sequenceName = "reos_id_seq", allocationSize = 1)
    private Integer id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Size(max = 200)
    @Column(length = 200)
    private String delito;

    @OneToMany(mappedBy = "reo", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Visitante> visitantes = new ArrayList<>();

    @OneToMany(mappedBy = "reo", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Visita> visitas = new ArrayList<>();

    @OneToMany(mappedBy = "reo", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Incidente> incidentes = new ArrayList<>();

    public Reo() {
    }

    public Reo(String nombre, String dni, String delito) {
        this.nombre = nombre;
        this.dni = dni;
        this.delito = delito;
    }

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

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDelito() {
        return delito;
    }

    public void setDelito(String delito) {
        this.delito = delito;
    }

    public List<Visitante> getVisitantes() {
        return visitantes;
    }

    public void setVisitantes(List<Visitante> visitantes) {
        this.visitantes = visitantes;
    }

    public List<Visita> getVisitas() {
        return visitas;
    }

    public void setVisitas(List<Visita> visitas) {
        this.visitas = visitas;
    }

    public List<Incidente> getIncidentes() {
        return incidentes;
    }

    public void setIncidentes(List<Incidente> incidentes) {
        this.incidentes = incidentes;
    }

    @Override
    public String toString() {
        return "Reo{id=" + id + ", nombre='" + nombre + "', dni='" + dni + "'}";
    }
}