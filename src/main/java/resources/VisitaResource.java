package resources;

import model.Reo;
import model.Visita;
import model.Visitante;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/visitas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VisitaResource {

    @Context
    private HttpServletRequest req;

    private static final String QR_PREFIX = "VISITA_";
    private static final Logger LOG = Logger.getLogger(VisitaResource.class.getName());

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    private boolean tieneRol(String... rolesPermitidos) {
        String rol = (String) req.getAttribute("rol");
        LOG.info("ROL LEIDO EN RESOURCE = '" + rol + "'");

        if (rol == null) {
            return false;
        }

        for (String permitido : rolesPermitidos) {
            if (rol.equalsIgnoreCase(permitido)) {
                LOG.info("ROL OK: " + rol);
                return true;
            }
        }

        LOG.warning("ROL DENEGADO: " + rol);
        return false;
    }

    @GET
    @Path("/mis-citas")
    public Response getMisCitas() {
        String username = (String) req.getAttribute("username");
        String rol = (String) req.getAttribute("rol");

        if (rol == null || !rol.equalsIgnoreCase("VISITANTE")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Acceso denegado"))
                    .build();
        }

        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Usuario no identificado"))
                    .build();
        }

        List<Visita> citas = em.createQuery(
                "SELECT v FROM Visita v LEFT JOIN FETCH v.reo "
                + "WHERE v.visitanteDni = :dni ORDER BY v.fechaVisita DESC",
                Visita.class)
                .setParameter("dni", username.trim())
                .getResultList();

        LOG.info("GET /api/visitas/mis-citas para DNI=" + username + ": " + citas.size() + " citas");
        return Response.ok(citas).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") Integer id) {
        Visita v = em.find(Visita.class, id);

        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe una visita con id: " + id))
                    .build();
        }

        return Response.ok(v).build();
    }

    @POST
    @Transactional
    public Response create(Visita visita) {
        LOG.info("POST /api/visitas: " + visita);

        try {
            if (visita == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "La visita no puede ser nula"))
                        .build();
            }

            if (visita.getReo() == null || visita.getReo().getId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El reo es obligatorio"))
                        .build();
            }

            Reo reo = em.find(Reo.class, visita.getReo().getId());
            if (reo == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No existe un reo con id: " + visita.getReo().getId()))
                        .build();
            }

            if (visita.getVisitanteDni() == null || visita.getVisitanteDni().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El visitanteDni es obligatorio"))
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
                        .entity(Map.of("error", "No existe un visitante registrado con DNI/NIE: " + visita.getVisitanteDni()))
                        .build();
            }

            if (visita.getFechaVisita() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "La fechaVisita es obligatoria"))
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
                    .entity(Map.of("error", "Error crear: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response update(@PathParam("id") Integer id, Visita visitaUpdate) {
        if (!tieneRol("ADMIN", "GUARDIA")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado"))
                    .build();
        }

        Visita v = em.find(Visita.class, id);
        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe una visita con id: " + id))
                    .build();
        }

        try {
            if (visitaUpdate == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Los datos de la visita son obligatorios"))
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
                            .entity(Map.of("error", "No existe un visitante registrado con DNI/NIE: " + visitaUpdate.getVisitanteDni()))
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
                            .entity(Map.of("error", "No existe un reo con id: " + visitaUpdate.getReo().getId()))
                            .build();
                }

                v.setReo(reo);
            }

            em.flush();
            return Response.ok(v).build();

        } catch (Exception e) {
            LOG.severe("Error PUT visita: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Error actualizar: " + e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        if (!tieneRol("ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado"))
                    .build();
        }

        Visita v = em.find(Visita.class, id);
        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe una visita con id: " + id))
                    .build();
        }

        em.remove(v);
        return Response.ok(Map.of("mensaje", "Visita eliminada correctamente")).build();
    }

    @POST
    @Path("qr/{id}")
    @Transactional
    public Response generarQR(@PathParam("id") Integer id) {
        String username = (String) req.getAttribute("username");
        String rol = (String) req.getAttribute("rol");

        if (rol == null || (!rol.equalsIgnoreCase("VISITANTE") && !rol.equalsIgnoreCase("ADMIN"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Solo visitantes o admin pueden generar QR"))
                    .build();
        }

        Visita v = em.find(Visita.class, id);
        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe una visita con id: " + id))
                    .build();
        }

        if (rol.equalsIgnoreCase("VISITANTE")) {
            if (username == null || username.trim().isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Usuario no identificado"))
                        .build();
            }

            if (v.getVisitanteDni() == null || !v.getVisitanteDni().equalsIgnoreCase(username.trim())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "No puedes generar QR para una visita que no es tuya"))
                        .build();
            }
        }

        String qrCode = QR_PREFIX + UUID.randomUUID().toString().substring(0, 8) + "_" + id;
        v.setCodigoQr(qrCode);

        em.merge(v);
        em.flush();

        LOG.info("QR generado para visita " + id + " por usuario " + username + ": " + qrCode);

        return Response.ok(Map.of(
                "mensaje", "QR generado correctamente",
                "qr", qrCode,
                "visitaId", v.getId()
        )).build();
    }

    @POST
    @Path("validar-qr")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validarQR(@FormParam("qr") String qrCode) {
        if (!tieneRol("GUARDIA", "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of(
                            "valido", false,
                            "mensaje", "Solo guardias o administradores pueden validar códigos QR"
                    ))
                    .build();
        }

        if (qrCode == null || qrCode.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "valido", false,
                            "mensaje", "El parámetro qr es obligatorio"
                    ))
                    .build();
        }

        List<Visita> visitas = em.createQuery(
                "SELECT v FROM Visita v WHERE v.codigoQr = :qr", Visita.class)
                .setParameter("qr", qrCode.trim())
                .getResultList();

        if (visitas.isEmpty()) {
            return Response.ok(Map.of(
                    "valido", false,
                    "mensaje", "QR no válido o no encontrado"
            )).build();
        }

        Visita v = visitas.get(0);

        if (Boolean.TRUE.equals(v.getAutorizado())) {
            return Response.ok(Map.of(
                    "valido", true,
                    "visitante", v.getVisitanteNombre(),
                    "mensaje", "QR validado correctamente"
            )).build();
        }

        return Response.ok(Map.of(
                "valido", false,
                "mensaje", "La visita existe, pero no está autorizada"
        )).build();
    }
}
