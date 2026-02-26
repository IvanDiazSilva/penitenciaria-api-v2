package com.ivan.penitenciaria.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/incidentes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IncidenteResource {

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    @GET
    public List<Incidente> getAllIncidentes() {
        return em.createQuery("SELECT i FROM Incidente i", Incidente.class)
                 .getResultList();
    }

    @GET
    @Path("/{id}")
    public Response getIncidente(@PathParam("id") Long id) {
        Incidente incidente = em.find(Incidente.class, id);
        if (incidente == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(incidente).build();
    }

    @POST
    @Transactional
    public Response createIncidente(Incidente incidente) {
        if (incidente.getTipo() == null || incidente.getDescripcion() == null
                || incidente.getIdGuardia() == null || incidente.getFechaHora() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Campos obligatorios: tipo, descripcion, idGuardia, fechaHora")
                           .build();
        }

        em.persist(incidente);
        return Response.status(Response.Status.CREATED).entity(incidente).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateIncidente(@PathParam("id") Long id, Incidente datos) {
        Incidente incidente = em.find(Incidente.class, id);
        if (incidente == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        incidente.setTipo(datos.getTipo());
        incidente.setDescripcion(datos.getDescripcion());
        incidente.setIdGuardia(datos.getIdGuardia());
        incidente.setFechaHora(datos.getFechaHora());
        incidente.setIdReo(datos.getIdReo());

        return Response.ok(incidente).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteIncidente(@PathParam("id") Long id) {
        Incidente incidente = em.find(Incidente.class, id);
        if (incidente == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        em.remove(incidente);
        return Response.ok().build();
    }
}
