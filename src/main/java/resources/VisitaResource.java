package resources;

import model.Reo;
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
import model.Visitante;

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
            if (visita == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La visita no puede ser nula")
                        .build();
            }

            if (visita.getReo() == null || visita.getReo().getId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El reo es obligatorio")
                        .build();
            }

            Reo reo = em.find(Reo.class, visita.getReo().getId());
            if (reo == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No existe un reo con id: " + visita.getReo().getId())
                        .build();
            }

            if (visita.getVisitanteDni() == null || visita.getVisitanteDni().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El visitanteDni es obligatorio")
                        .build();
            }

            Visitante visitante = em.createQuery(
                    "SELECT v FROM Visitante v WHERE v.dniNie = :dni", Visitante.class)
                    .setParameter("dni", visita.getVisitanteDni().trim())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (visitante == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No existe un visitante registrado con DNI/NIE: " + visita.getVisitanteDni())
                        .build();
            }

            if (visita.getFechaVisita() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La fechaVisita es obligatoria")
                        .build();
            }

            if (visita.getVisitanteNombre() == null || visita.getVisitanteNombre().trim().isEmpty()) {
                visita.setVisitanteNombre(visitante.getNombreCompleto());
            }

            visita.setVisitanteDni(visita.getVisitanteDni().trim());
            visita.setReo(reo);

            if (visita.getAutorizado() == null) {
                visita.setAutorizado(true);
            }

            em.persist(visita);
            em.flush();

            return Response.status(Response.Status.CREATED).entity(visita).build();

        } catch (Exception e) {
            LOG.severe("Error POST visita: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error crear: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response update(@PathParam("id") Integer id, Visita visitaUpdate,
            @Context HttpServletRequest req) {

        if (!autorizadoVisitas(req)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Visita v = em.find(Visita.class, id);
        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No existe una visita con id: " + id)
                    .build();
        }

        try {
            if (visitaUpdate == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Los datos de la visita son obligatorios")
                        .build();
            }

            if (visitaUpdate.getVisitanteNombre() != null
                    && !visitaUpdate.getVisitanteNombre().trim().isEmpty()) {
                v.setVisitanteNombre(visitaUpdate.getVisitanteNombre().trim());
            }

            if (visitaUpdate.getVisitanteDni() != null
                    && !visitaUpdate.getVisitanteDni().trim().isEmpty()) {

                Visitante visitante = em.createQuery(
                        "SELECT vt FROM Visitante vt WHERE vt.dniNie = :dni", Visitante.class)
                        .setParameter("dni", visitaUpdate.getVisitanteDni().trim())
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                if (visitante == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("No existe un visitante registrado con DNI/NIE: "
                                    + visitaUpdate.getVisitanteDni())
                            .build();
                }

                v.setVisitanteDni(visitaUpdate.getVisitanteDni().trim());

                if (visitaUpdate.getVisitanteNombre() == null
                        || visitaUpdate.getVisitanteNombre().trim().isEmpty()) {
                    v.setVisitanteNombre(visitante.getNombreCompleto());
                }
            }

            if (visitaUpdate.getFechaVisita() != null) {
                v.setFechaVisita(visitaUpdate.getFechaVisita());
            }

            if (visitaUpdate.getHoraEntrada() != null) {
                v.setHoraEntrada(visitaUpdate.getHoraEntrada());
            }

            if (visitaUpdate.getHoraSalida() != null) {
                v.setHoraSalida(visitaUpdate.getHoraSalida());
            }

            if (visitaUpdate.getAutorizado() != null) {
                v.setAutorizado(visitaUpdate.getAutorizado());
            }

            if (visitaUpdate.getCodigoQr() != null
                    && !visitaUpdate.getCodigoQr().trim().isEmpty()) {
                v.setCodigoQr(visitaUpdate.getCodigoQr().trim());
            }

            if (visitaUpdate.getReo() != null && visitaUpdate.getReo().getId() != null) {
                Reo reo = em.find(Reo.class, visitaUpdate.getReo().getId());

                if (reo == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("No existe un reo con id: " + visitaUpdate.getReo().getId())
                            .build();
                }

                v.setReo(reo);
            }

            em.flush();

            return Response.ok(v).build();

        } catch (Exception e) {
            LOG.severe("Error PUT visita: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error actualizar: " + e.getMessage())
                    .build();
        }
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
