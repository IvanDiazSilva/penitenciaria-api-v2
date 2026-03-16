package resources;  // ← TU PACKAGE (no rest)

import model.Visitante;  // ← TU PACKAGE model (como VisitaResource)
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;

@Path("/visitantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VisitanteResource {

    private static final Logger LOG = Logger.getLogger(VisitanteResource.class.getName());

    @PersistenceContext(unitName = "PenitenciariaPU")  // ← EXACTO como VisitaResource
    private EntityManager em;

    @POST
    @Transactional
    public Response create(Visitante visitante) {
        LOG.info("POST /api/visitantes: " + visitante);
        try {
            em.persist(visitante);
            em.flush();
            return Response.status(Response.Status.CREATED).entity(visitante).build();
        } catch (Exception e) {
            LOG.severe("Error POST visitante: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error crear: " + e.getMessage()).build();
        }
    }

    @GET
    public Response getAll() {
        List<Visitante> visitantes = em.createQuery(
                "SELECT v FROM Visitante v ORDER BY v.apellidos, v.nombre",
                Visitante.class
        ).getResultList();
        LOG.info("GET /api/visitantes: " + visitantes.size() + " visitantes");
        return Response.ok(visitantes).build();
    }

}
