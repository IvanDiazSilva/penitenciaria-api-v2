package dto;

public class CrearVisitaRequest {
    private Integer reoId;
    private String fechaVisita;
    private String horaEntrada;
    private String horaSalida;

    public Integer getReoId() {
        return reoId;
    }

    public void setReoId(Integer reoId) {
        this.reoId = reoId;
    }

    public String getFechaVisita() {
        return fechaVisita;
    }

    public void setFechaVisita(String fechaVisita) {
        this.fechaVisita = fechaVisita;
    }

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }
}