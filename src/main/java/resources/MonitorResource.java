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

}
