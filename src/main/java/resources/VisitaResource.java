package resources;

import model.Visita;  // Ajusta tu package model
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import java.util.List;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import java.util.Base64;
import java.util.UUID;

@Path("/visitas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VisitaResource {

    private final String qrPrefix = "VISITA_";
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
    public Response update(@PathParam("id") Integer id, Visita visitaUpdate,
            @Context HttpServletRequest req) {
        if (!autorizadoVisitas(req)) {
            return Response.status(403).build();
        }

        Visita v = em.find(Visita.class, id);
        if (v == null) {
            return Response.status(404).build();
        }

        // Solo campos simples
        v.setVisitanteNombre(visitaUpdate.getVisitanteNombre());
        v.setAutorizado(visitaUpdate.getAutorizado());
        // NO: v.setReo() ← evita FK

        em.merge(v);
        return Response.ok(v).build();
    }

    private boolean autorizadoVisitas(@Context HttpServletRequest req) {
        String rol = (String) req.getAttribute("rol");
        return rol != null;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        if (!autorizadoVisitas(req)) {
            return Response.status(403).build();  // ← AÑADIR
        }
        Visita v = em.find(Visita.class, id);
        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        em.remove(v);
        return Response.ok().build();
    }

    @POST
    @Path("qr/{id}")
    @Transactional
    public Response generarQR(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        if (!autorizadoVisitas(req) || !"VISITANTE".equalsIgnoreCase((String) req.getAttribute("rol"))) {
            return Response.status(403).build();
        }

        Visita v = em.find(Visita.class, id);
        if (v == null) {
            return Response.status(404).build();
        }

        // Genera QR simple (UUID + visita ID)
        String qrCode = qrPrefix + UUID.randomUUID().toString().substring(0, 8) + "_" + id;
        v.setCodigoQr(qrCode);  // Añade campo codigoQr a Visita.java
        em.merge(v);

        return Response.ok().entity("{\"qr\":\"" + qrCode + "\"}").build();
    }

    @POST
    @Path("validar-qr")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response validarQR(@FormParam("qr") String qrCode, @Context HttpServletRequest req) {
        if (!autorizadoVisitas(req) || !"GUARDIA".equalsIgnoreCase((String) req.getAttribute("rol"))) {
            return Response.status(403).build();
        }

        Visita v = em.createQuery("SELECT v FROM Visita v WHERE v.codigoQr = :qr", Visita.class)
                .setParameter("qr", qrCode)
                .getSingleResult();

        if (v != null && v.getAutorizado()) {
            return Response.ok().entity("{\"valido\":true,\"visitante\":\"" + v.getVisitanteNombre() + "\"}").build();
        }
        return Response.status(404).entity("{\"valido\":false}").build();
    }

}
