package com.ivan.penitenciaria.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;   // <-- IMPORTANTE
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
        return Response.ok(visitas).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") int id) {
        Visita v = em.find(Visita.class, id);
        return v != null ? Response.ok(v).build()
                         : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Transactional     // <-- EVITA WFLYJPA0060
    public Response create(Visita visita) {
        LOG.info("POST /api/visitas: " + visita);
        em.persist(visita);
        em.flush();
        return Response.status(Response.Status.CREATED).entity(visita).build();
    }
}
