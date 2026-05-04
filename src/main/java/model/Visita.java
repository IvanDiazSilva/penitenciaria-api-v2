package model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "visitas")
@NamedQuery(name = "Visita.findAll", query = "SELECT v FROM Visita v ORDER BY v.fechaVisita DESC")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visitas_seq")
    @SequenceGenerator(name = "visitas_seq", sequenceName = "visitas_id_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reo_id", nullable = false)
    private Reo reo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "visitante_id", nullable = false)
    private Visitante visitante;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "fecha_visita", nullable = false)
    private LocalDate fechaVisita;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "autorizado")
    private Boolean autorizado = true;

    @Column(name = "codigo_qr", length = 100)
    private String codigoQr;

    // --- CAMPOS PARA EL FLUJO QR ---
    @Column(name = "validada")
    private Boolean validada = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;

    public Visita() {
    }

    // --- GETTERS Y SETTERS ---
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

    public Visitante getVisitante() {
        return visitante;
    }

    public void setVisitante(Visitante visitante) {
        this.visitante = visitante;
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

    public Boolean getValidada() {
        return validada;
    }

    public void setValidada(Boolean validada) {
        this.validada = validada;
    }

    public LocalDateTime getFechaValidacion() {
        return fechaValidacion;
    }

    public void setFechaValidacion(LocalDateTime fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }
}
