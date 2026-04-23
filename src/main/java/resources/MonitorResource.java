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
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.awt.Color;

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

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font cabeceraFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font celdaFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            Paragraph titulo = new Paragraph("Informe del sistema penitenciario", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(10f);
            document.add(titulo);

            Paragraph fecha = new Paragraph("Fecha de generación: " + hoy, subtituloFont);
            fecha.setAlignment(Element.ALIGN_CENTER);
            fecha.setSpacingAfter(20f);
            document.add(fecha);

            Paragraph resumen = new Paragraph("Resumen general", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
            resumen.setSpacingAfter(10f);
            document.add(resumen);

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10f);
            tabla.setSpacingAfter(10f);
            tabla.setWidths(new float[]{3f, 2f});

            PdfPCell cabecera1 = new PdfPCell(new Phrase("Campo", cabeceraFont));
            cabecera1.setBackgroundColor(new Color(52, 73, 94));
            cabecera1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cabecera1.setPadding(8f);

            PdfPCell cabecera2 = new PdfPCell(new Phrase("Valor", cabeceraFont));
            cabecera2.setBackgroundColor(new Color(52, 73, 94));
            cabecera2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cabecera2.setPadding(8f);

            tabla.addCell(cabecera1);
            tabla.addCell(cabecera2);

            tabla.addCell(crearCelda("Total de reclusos", celdaFont));
            tabla.addCell(crearCelda(String.valueOf(totalReos()), celdaFont));

            tabla.addCell(crearCelda("Total de visitas", celdaFont));
            tabla.addCell(crearCelda(String.valueOf(totalVisitas()), celdaFont));

            tabla.addCell(crearCelda("Visitas de hoy", celdaFont));
            tabla.addCell(crearCelda(String.valueOf(totalVisitasHoy(hoy)), celdaFont));

            tabla.addCell(crearCelda("Total de incidentes", celdaFont));
            tabla.addCell(crearCelda(String.valueOf(totalIncidentes()), celdaFont));

            document.add(tabla);

            Paragraph nota = new Paragraph(
                    "Documento generado automáticamente por el módulo de monitor.",
                    subtituloFont
            );
            nota.setSpacingBefore(10f);
            document.add(nota);

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

    private PdfPCell crearCelda(String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setPadding(8f);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return celda;
    }
}
