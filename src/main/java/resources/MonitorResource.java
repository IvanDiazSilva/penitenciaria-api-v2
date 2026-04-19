package resources;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Path("/monitor")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class MonitorResource {

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    private boolean autorizado(@Context HttpServletRequest req) {
        String rol = (String) req.getAttribute("rol");
        return "ADMIN".equals(rol) || "GUARDIA".equals(rol);
    }

    @GET  // ← /api/monitor
    public Response dashboard(@Context HttpServletRequest req) {
        if (!autorizado(req)) {
            return Response.status(403).build();
        }
        Map<String, Long> stats = new HashMap<>();
        stats.put("reoTotal", em.createQuery("SELECT COUNT(r) FROM Reo r", Long.class).getSingleResult());
        stats.put("visitasTotal", em.createQuery("SELECT COUNT(v) FROM Visita v", Long.class).getSingleResult());
        stats.put("incidentesTotal", em.createQuery("SELECT COUNT(i) FROM Incidente i", Long.class).getSingleResult());
        return Response.ok(stats).build();
    }

    @GET
    @Path("/visitas-hoy")
    public Response visitasHoy(@Context HttpServletRequest req) {
        if (!autorizado(req)) {
            return Response.status(403).build();
        }

        LocalDate hoy = LocalDate.now();
        Long total = em.createQuery("SELECT COUNT(v) FROM Visita v WHERE v.fechaVisita = :hoy", Long.class)
                .setParameter("hoy", hoy).getSingleResult();

        Long autorizados = em.createQuery("SELECT COUNT(v) FROM Visita v WHERE v.fechaVisita = :hoy AND v.autorizado = true", Long.class)
                .setParameter("hoy", hoy).getSingleResult();

        Map<String, Object> stats = new HashMap<>();
        stats.put("fecha", hoy.toString());
        stats.put("total", total);
        stats.put("autorizados", autorizados);  // ← autorizados

        return Response.ok(stats).build();
    }

    @GET
    @Path("/informe")
    public Response informe(@Context HttpServletRequest req) {
        if (!autorizado(req)) {
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "No autorizado");
            return Response.status(Response.Status.FORBIDDEN).entity(error).build();
        }

        LocalDate hoy = LocalDate.now();

        Long reos = em.createQuery("SELECT COUNT(r) FROM Reo r", Long.class).getSingleResult();
        Long visitas = em.createQuery("SELECT COUNT(v) FROM Visita v", Long.class).getSingleResult();
        Long incidentes = em.createQuery("SELECT COUNT(i) FROM Incidente i", Long.class).getSingleResult();
        Long visitasHoy = em.createQuery("SELECT COUNT(v) FROM Visita v WHERE v.fechaVisita = :hoy", Long.class)
                .setParameter("hoy", hoy)
                .getSingleResult();

        Map<String, Object> informe = new HashMap<>();
        informe.put("mensaje", "Informe generado correctamente");
        informe.put("fecha", hoy.toString());
        informe.put("reoTotal", reos);
        informe.put("visitasTotal", visitas);
        informe.put("incidentesTotal", incidentes);
        informe.put("visitasHoy", visitasHoy);

        return Response.ok(informe).build();
    }

    @GET
    @Path("/informe/pdf")
    @Produces("application/pdf")
    public Response descargarInformePdf(@Context HttpServletRequest req) {
        if (!autorizado(req)) {
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "No autorizado");
            return Response.status(Response.Status.FORBIDDEN).entity(error).build();
        }

        try {
            LocalDate hoy = LocalDate.now();

            Long reos = em.createQuery("SELECT COUNT(r) FROM Reo r", Long.class).getSingleResult();
            Long visitas = em.createQuery("SELECT COUNT(v) FROM Visita v", Long.class).getSingleResult();
            Long incidentes = em.createQuery("SELECT COUNT(i) FROM Incidente i", Long.class).getSingleResult();
            Long visitasHoy = em.createQuery("SELECT COUNT(v) FROM Visita v WHERE v.fechaVisita = :hoy", Long.class)
                    .setParameter("hoy", hoy)
                    .getSingleResult();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();
            document.add(new Paragraph("Informe del sistema penitenciario"));
            document.add(new Paragraph("Fecha: " + hoy));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total de reclusos: " + reos));
            document.add(new Paragraph("Total de visitas: " + visitas));
            document.add(new Paragraph("Visitas de hoy: " + visitasHoy));
            document.add(new Paragraph("Total de incidentes: " + incidentes));
            document.close();

            return Response.ok(baos.toByteArray())
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
