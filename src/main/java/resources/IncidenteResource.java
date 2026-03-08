package resources;

import model.Incidente;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;

@Path("/incidentes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IncidenteResource {

    private static final Logger LOG = Logger.getLogger(IncidenteResource.class.getName());

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    private boolean autorizadoIncidentes(@Context HttpServletRequest req) {
        String rol = (String) req.getAttribute("rol");  // "ADMIN"
        return rol != null && rol.equalsIgnoreCase("admin");  // Case insensitive
    }

    @GET
    public Response getAll(@Context HttpServletRequest req) {
        if (!autorizadoIncidentes(req)) {
            return Response.status(403).entity("{\"error\":\"Rol incidentes/admin/guardia requerido\"}").build();
        }
        List<Incidente> incidentes = em.createQuery(
                "SELECT i FROM Incidente i ORDER BY i.fechaHora DESC", Incidente.class)
                .setMaxResults(100) // Limit seguridad
                .getResultList();
        LOG.info("GET /api/incidentes (" + incidentes.size() + ")");
        return Response.ok(incidentes).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") Long id, @Context HttpServletRequest req) {
        if (!autorizadoIncidentes(req)) {
            return Response.status(403).build();
        }
        Incidente i = em.find(Incidente.class, id);
        return i != null ? Response.ok(i).build() : Response.status(404).build();
    }

    @POST
    @Transactional
    public Response create(Incidente incidente, @Context HttpServletRequest req) {
        if (!autorizadoIncidentes(req)) {
            return Response.status(403).entity("{\"error\":\"No autorizado crear incidentes\"}").build();
        }
        LOG.info("POST /api/incidentes: " + incidente);
        try {
            em.persist(incidente);
            em.flush();
            return Response.status(201).entity(incidente).build();
        } catch (Exception e) {
            LOG.severe("Error POST: " + e.getMessage());
            return Response.status(400).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, Incidente update, @Context HttpServletRequest req) {
        if (!autorizadoIncidentes(req)) {
            return Response.status(403).build();
        }
        Incidente i = em.find(Incidente.class, id);
        if (i == null) {
            return Response.status(404).build();
        }
        i.setDescripcion(update.getDescripcion());
        i.setTipo(update.getTipo());
        em.merge(i);
        return Response.ok(i).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id, @Context HttpServletRequest req) {
        if (!autorizadoIncidentes(req)) {
            return Response.status(403).build();
        }
        Incidente i = em.find(Incidente.class, id);
        if (i == null) {
            return Response.status(404).build();
        }
        em.remove(i);
        return Response.ok().build();
    }
}
