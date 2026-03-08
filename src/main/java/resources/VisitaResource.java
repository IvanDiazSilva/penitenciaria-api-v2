package resources;

import model.Visita;  // Ajusta tu package model
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;

@Path("/visitas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VisitaResource {

    private static final Logger LOG = Logger.getLogger(VisitaResource.class.getName());

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    @GET
    public Response getAll() {
        List<Visita> visitas = em.createQuery(
                "SELECT v FROM Visita v LEFT JOIN FETCH v.reo ORDER BY v.fechaVisita DESC",
                Visita.class
        ).getResultList();
        LOG.info("GET /api/visitas: " + visitas.size() + " visitas");
        return Response.ok(visitas).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") Integer id) {
        Visita v = em.find(Visita.class, id);
        return v != null ? Response.ok(v).build()
                         : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Transactional
    public Response create(Visita visita) {
        LOG.info("POST /api/visitas: " + visita);
        try {
            em.persist(visita);
            em.flush();
            return Response.status(Response.Status.CREATED).entity(visita).build();
        } catch (Exception e) {
            LOG.severe("Error POST visita: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("Error crear: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response update(@PathParam("id") Integer id, Visita visitaUpdate) {
        Visita v = em.find(Visita.class, id);
        if (v == null) return Response.status(Response.Status.NOT_FOUND).build();
        v.setVisitanteNombre(visitaUpdate.getVisitanteNombre());
        v.setFechaVisita(visitaUpdate.getFechaVisita());
        v.setAutorizado(visitaUpdate.getAutorizado());
        // Más campos si necesitas
        em.merge(v);
        return Response.ok(v).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        Visita v = em.find(Visita.class, id);
        if (v == null) return Response.status(Response.Status.NOT_FOUND).build();
        em.remove(v);
        return Response.ok().build();
    }
}