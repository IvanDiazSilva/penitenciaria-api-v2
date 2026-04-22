package resources;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Path("/monitor")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class MonitorResource {

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    private boolean tieneAccesoMonitor(HttpServletRequest req) {
        String rol = (String) req.getAttribute("rol");
        return "ADMIN".equals(rol) || "GUARDIA".equals(rol);
    }

    private Response forbidden() {
        Map<String, Object> error = new HashMap<>();
        error.put("mensaje", "No autorizado");
        return Response.status(Response.Status.FORBIDDEN).entity(error).build();
    }

    private Long totalReos() {
        return em.createQuery("SELECT COUNT(r) FROM Reo r", Long.class)
                .getSingleResult();
    }

    private Long totalVisitas() {
        return em.createQuery("SELECT COUNT(v) FROM Visita v", Long.class)
                .getSingleResult();
    }

    private Long totalIncidentes() {
        return em.createQuery("SELECT COUNT(i) FROM Incidente i", Long.class)
                .getSingleResult();
    }

    private Long totalVisitasHoy(LocalDate hoy) {
        return em.createQuery(
                "SELECT COUNT(v) FROM Visita v WHERE v.fechaVisita = :hoy",
                Long.class)
                .setParameter("hoy", hoy)
                .getSingleResult();
    }

    private Long totalVisitasHoyAutorizadas(LocalDate hoy) {
        return em.createQuery(
                "SELECT COUNT(v) FROM Visita v WHERE v.fechaVisita = :hoy AND v.autorizado = true",
                Long.class)
                .setParameter("hoy", hoy)
                .getSingleResult();
    }

    @GET
    public Response dashboard(@Context HttpServletRequest req) {
        if (!tieneAccesoMonitor(req)) {
            return forbidden();
        }

        Map<String, Long> stats = new HashMap<>();
        stats.put("reoTotal", totalReos());
        stats.put("visitasTotal", totalVisitas());
        stats.put("incidentesTotal", totalIncidentes());

        return Response.ok(stats).build();
    }

    @GET
    @Path("/visitas-hoy")
    public Response visitasHoy(@Context HttpServletRequest req) {
        if (!tieneAccesoMonitor(req)) {
            return forbidden();
        }

        LocalDate hoy = LocalDate.now();

        Map<String, Object> stats = new HashMap<>();
        stats.put("fecha", hoy.toString());
        stats.put("total", totalVisitasHoy(hoy));
        stats.put("autorizados", totalVisitasHoyAutorizadas(hoy));

        return Response.ok(stats).build();
    }

    @GET
    @Path("/informe")
    public Response informe(@Context HttpServletRequest req) {
        if (!tieneAccesoMonitor(req)) {
            return forbidden();
        }

        LocalDate hoy = LocalDate.now();

        Map<String, Object> informe = new HashMap<>();
        informe.put("mensaje", "Informe generado correctamente");
        informe.put("fecha", hoy.toString());
        informe.put("reoTotal", totalReos());
        informe.put("visitasTotal", totalVisitas());
        informe.put("incidentesTotal", totalIncidentes());
        informe.put("visitasHoy", totalVisitasHoy(hoy));

        return Response.ok(informe).build();
    }

    @GET
    @Path("/informe/pdf")
    @Produces("application/pdf")
    public Response descargarInformePdf(@Context HttpServletRequest req) {
        if (!tieneAccesoMonitor(req)) {
            return forbidden();
        }

        try {
            LocalDate hoy = LocalDate.now();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();
            document.add(new Paragraph("Informe del sistema penitenciario"));
            document.add(new Paragraph("Fecha: " + hoy));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total de reclusos: " + totalReos()));
            document.add(new Paragraph("Total de visitas: " + totalVisitas()));
            document.add(new Paragraph("Visitas de hoy: " + totalVisitasHoy(hoy)));
            document.add(new Paragraph("Total de incidentes: " + totalIncidentes()));
            document.close();

            return Response.ok(baos.toByteArray(), "application/pdf")
                    .header("Content-Disposition", "attachment; filename=informe-penitenciaria.pdf")
                    .build();

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Error al generar el PDF");
            error.put("detalle", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}