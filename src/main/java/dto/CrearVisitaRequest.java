package dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CrearVisitaRequest {

    private Integer reoId;
    private Integer visitanteId;
    private LocalDate fechaVisita;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    public CrearVisitaRequest() {
    }

    public Integer getReoId() {
        return reoId;
    }

    public void setReoId(Integer reoId) {
        this.reoId = reoId;
    }

    public Integer getVisitanteId() {
        return visitanteId;
    }

    public void setVisitanteId(Integer visitanteId) {
        this.visitanteId = visitanteId;
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
}