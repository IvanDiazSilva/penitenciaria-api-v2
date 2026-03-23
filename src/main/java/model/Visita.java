package model;

import model.Reo;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "visitas")
@NamedQuery(name = "Visita.findAll", query = "SELECT v FROM Visita v ORDER BY v.fechaVisita DESC")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reo_id", nullable = false)
    private Reo reo;

    @Column(name = "visitante_nombre", length = 100, nullable = false)
    private String visitanteNombre;

    @Column(name = "visitante_dni", length = 9, nullable = false)
    private String visitanteDni;

    @Column(name = "fecha_visita", nullable = false)
    private LocalDate fechaVisita;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "autorizado")
    private Boolean autorizado = true;

    @Column(name = "codigo_qr", length = 100)
    private String codigoQr;

    // Constructores
    public Visita() {
    }

    // Getters/Setters (NetBeans: Insert Code → Getter/Setter)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Reo getReo() {
        return reo;
    }

    public void setReo(Reo reo) {
        this.reo = reo;
    }

    public String getVisitanteNombre() {
        return visitanteNombre;
    }

    public void setVisitanteNombre(String visitanteNombre) {
        this.visitanteNombre = visitanteNombre;
    }

    public String getVisitanteDni() {
        return visitanteDni;
    }

    public void setVisitanteDni(String visitanteDni) {
        this.visitanteDni = visitanteDni;
    }

    public LocalDate getFechaVisita() {
        return fechaVisita;
    }

    public void setFechaVisita(LocalDate fechaVisita) {
        this.fechaVisita = fechaVisita;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Boolean getAutorizado() {
        return autorizado;
    }

    public void setAutorizado(Boolean autorizado) {
        this.autorizado = autorizado;
    }

    public String getCodigoQr() {
        return codigoQr;
    }

    public void setCodigoQr(String codigoQr) {
        this.codigoQr = codigoQr;
    }
    
    
    

}
