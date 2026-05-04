package resources;

import dto.CrearVisitaRequest;
import model.Reo;
import model.Usuario;
import model.Visita;
import model.Visitante;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
                "SELECT DISTINCT v FROM Visita v "
                + "LEFT JOIN FETCH v.reo "
                + "LEFT JOIN FETCH v.visitante "
                + "JOIN v.visitante vt "
                + "JOIN vt.usuario u "
                + "WHERE u.username = :username "
                + "ORDER BY v.fechaVisita DESC",
                Visita.class)
                .setParameter("username", username.trim())
                .getResultList();

        LOG.info("GET /api/visitas/mis-citas para username=" + username + ": " + citas.size() + " citas");
        return Response.ok(citas).build();
    }

    @GET
    public Response getAllVisitas() {
        if (!tieneRol("ADMIN", "GUARDIA")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado"))
                    .build();
        }

        List<Visita> visitas = em.createQuery(
                "SELECT DISTINCT v FROM Visita v "
                + "LEFT JOIN FETCH v.reo "
                + "LEFT JOIN FETCH v.visitante "
                + "ORDER BY v.fechaVisita DESC, v.id DESC",
                Visita.class)
                .getResultList();

        LOG.info("GET /api/visitas -> " + visitas.size() + " visitas");
        return Response.ok(visitas).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") Integer id) {
        Visita v = em.createQuery(
                "SELECT v FROM Visita v "
                + "LEFT JOIN FETCH v.reo "
                + "LEFT JOIN FETCH v.visitante "
                + "WHERE v.id = :id",
                Visita.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe una visita con id: " + id))
                    .build();
        }

        return Response.ok(v).build();
    }

    @POST
    @Transactional
    public Response create(CrearVisitaRequest request) {
        LOG.info("POST /api/visitas con DTO");

        String username = (String) req.getAttribute("username");
        String rol = (String) req.getAttribute("rol");

        if (rol == null || !rol.equalsIgnoreCase("VISITANTE")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Solo los visitantes pueden solicitar visitas"))
                    .build();
        }

        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Usuario no identificado"))
                    .build();
        }

        try {
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El cuerpo de la petición es obligatorio"))
                        .build();
            }

            if (request.getReoId() == null || request.getReoId() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El reo es obligatorio"))
                        .build();
            }

            if (request.getFechaVisita() == null || request.getFechaVisita().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "La fecha de visita es obligatoria"))
                        .build();
            }

            Usuario usuario = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.username = :username",
                    Usuario.class)
                    .setParameter("username", username.trim())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Usuario autenticado no encontrado"))
                        .build();
            }

            Visitante visitante = em.createQuery(
                    "SELECT v FROM Visitante v WHERE v.usuario.id = :usuarioId",
                    Visitante.class)
                    .setParameter("usuarioId", usuario.getId())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (visitante == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No existe un visitante asociado al usuario autenticado"))
                        .build();
            }

            Reo reo = em.find(Reo.class, request.getReoId());
            if (reo == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El reo no existe"))
                        .build();
            }

            Visita nuevaVisita = new Visita();
            nuevaVisita.setReo(reo);
            nuevaVisita.setVisitante(visitante);
            nuevaVisita.setFechaVisita(LocalDate.parse(request.getFechaVisita()));
            nuevaVisita.setHoraEntrada(
                    request.getHoraEntrada() != null && !request.getHoraEntrada().isBlank()
                    ? LocalTime.parse(request.getHoraEntrada())
                    : null
            );
            nuevaVisita.setHoraSalida(
                    request.getHoraSalida() != null && !request.getHoraSalida().isBlank()
                    ? LocalTime.parse(request.getHoraSalida())
                    : null
            );
            nuevaVisita.setAutorizado(false);
            nuevaVisita.setCodigoQr(null);

            em.persist(nuevaVisita);
            em.flush();

            LOG.info("Visita creada correctamente. ID=" + nuevaVisita.getId()
                    + ", username=" + username
                    + ", visitanteId=" + visitante.getId()
                    + ", reoId=" + reo.getId());

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                            "mensaje", "Solicitud de visita creada correctamente",
                            "id", nuevaVisita.getId(),
                            "reoId", reo.getId(),
                            "visitanteId", visitante.getId(),
                            "fechaVisita", nuevaVisita.getFechaVisita().toString(),
                            "horaEntrada", nuevaVisita.getHoraEntrada() != null ? nuevaVisita.getHoraEntrada().toString() : null,
                            "horaSalida", nuevaVisita.getHoraSalida() != null ? nuevaVisita.getHoraSalida().toString() : null,
                            "autorizado", nuevaVisita.getAutorizado()
                    ))
                    .build();

        } catch (Exception e) {
            LOG.severe("Error POST visita: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Error al crear la visita: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response update(@PathParam("id") Integer id, Visita visitaUpdate) {
        String username = (String) req.getAttribute("username");
        String rol = (String) req.getAttribute("rol");

        if (rol == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Usuario no identificado"))
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

            String ownerUsername = v.getVisitante() != null
                    && v.getVisitante().getUsuario() != null
                    ? v.getVisitante().getUsuario().getUsername()
                    : null;

            if (ownerUsername == null || !ownerUsername.equalsIgnoreCase(username.trim())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "No puedes modificar una visita que no es tuya"))
                        .build();
            }
        } else if (!rol.equalsIgnoreCase("ADMIN") && !rol.equalsIgnoreCase("GUARDIA")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado"))
                    .build();
        }

        try {
            if (visitaUpdate == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Los datos de la visita son obligatorios"))
                        .build();
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

            if (visitaUpdate.getVisitante() != null && visitaUpdate.getVisitante().getId() != null) {
                Visitante visitante = em.find(Visitante.class, visitaUpdate.getVisitante().getId());

                if (visitante == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("error", "No existe un visitante con id: " + visitaUpdate.getVisitante().getId()))
                            .build();
                }

                if (rol.equalsIgnoreCase("VISITANTE")) {
                    String ownerUsername = visitante.getUsuario() != null ? visitante.getUsuario().getUsername() : null;
                    if (ownerUsername == null || !ownerUsername.equalsIgnoreCase(username.trim())) {
                        return Response.status(Response.Status.FORBIDDEN)
                                .entity(Map.of("error", "No puedes reasignar la visita a otro visitante"))
                                .build();
                    }
                }

                v.setVisitante(visitante);
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

        if (!Boolean.TRUE.equals(v.getAutorizado())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "La visita no está autorizada"))
                    .build();
        }

        if (rol.equalsIgnoreCase("VISITANTE")) {
            if (username == null || username.trim().isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Usuario no identificado"))
                        .build();
            }
            String ownerUsername = v.getVisitante() != null && v.getVisitante().getUsuario() != null
                    ? v.getVisitante().getUsuario().getUsername() : null;

            if (ownerUsername == null || !ownerUsername.equalsIgnoreCase(username.trim())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "No puedes generar QR para una visita que no es tuya"))
                        .build();
            }
        }

        String qrCode = "VISITA_" + UUID.randomUUID().toString().substring(0, 8) + "_" + id;
        v.setCodigoQr(qrCode);
        v.setValidada(false); // Reset para permitir nueva validación
        v.setFechaValidacion(null);

        em.merge(v);
        em.flush();

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
    @Transactional // Asegúrate de tener esto para persistir el cambio de 'validada'
    public Response validarQR(@FormParam("qr") String qrCode) {
        if (!tieneRol("GUARDIA", "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("valido", false, "mensaje", "No autorizado"))
                    .build();
        }

        if (qrCode == null || qrCode.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("valido", false, "mensaje", "QR obligatorio"))
                    .build();
        }

        List<Visita> visitas = em.createQuery(
                "SELECT v FROM Visita v LEFT JOIN FETCH v.visitante WHERE v.codigoQr = :qr", Visita.class)
                .setParameter("qr", qrCode.trim())
                .getResultList();

        if (visitas.isEmpty()) {
            return Response.ok(Map.of("valido", false, "mensaje", "QR no encontrado")).build();
        }

        Visita v = visitas.get(0);

        if (!Boolean.TRUE.equals(v.getAutorizado())) {
            return Response.ok(Map.of("valido", false, "mensaje", "Visita no autorizada")).build();
        }

        // Comprobar si ya se validó
        if (Boolean.TRUE.equals(v.getValidada())) {
            return Response.ok(Map.of("valido", false, "mensaje", "El QR ya fue usado")).build();
        }

        // Marcar como validada
        v.setValidada(true);
        v.setFechaValidacion(LocalDateTime.now());

        em.merge(v);
        em.flush();

        return Response.ok(Map.of(
                "valido", true,
                "visitante", v.getVisitante() != null ? v.getVisitante().getNombreCompleto() : "Desconocido",
                "mensaje", "Acceso concedido"
        )).build();
    }
}
