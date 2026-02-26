package com.ivan.penitenciaria.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/reos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ReoResource {
    
    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    @GET
    public List<Reo> getAllReos() {
        return em.createQuery("SELECT r FROM Reo r", Reo.class).getResultList();
    }

    @GET
    @Path("/{id}")
    public Response getReo(@PathParam("id") Long id) {
        Reo reo = em.find(Reo.class, id);
        return reo != null ? Response.ok(reo).build() : Response.status(404).build();
    }

    @POST
    @Transactional
    public Response crearReo(Reo reo) {
        em.persist(reo);
        return Response.status(201).entity(reo).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response actualizarReo(@PathParam("id") Long id, Reo reoUpdate) {
        Reo reo = em.find(Reo.class, id);
        if (reo == null) return Response.status(404).build();
        reo.setNombre(reoUpdate.getNombre());
        reo.setDni(reoUpdate.getDni());
        reo.setDelito(reoUpdate.getDelito());
        em.merge(reo);
        return Response.ok(reo).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response eliminarReo(@PathParam("id") Long id) {
        Reo reo = em.find(Reo.class, id);
        if (reo == null) return Response.status(404).build();
        em.remove(reo);
        return Response.ok().build();
    }
}
