package resources;

import dto.CrearVisitaRequest;
import model.Reo;
import model.Usuario;
import model.Visita;
import model.Visitante;

import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
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

    private static final Logger LOG = Logger.getLogger(VisitaResource.class.getName());

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    @Context
    private SecurityContext securityContext;

    private String getUsername() {
        Principal principal = securityContext != null ? securityContext.getUserPrincipal() : null;
        return principal != null ? principal.getName() : null;
    }

    private boolean esAdmin() {
        return securityContext != null && securityContext.isUserInRole("ADMIN");
    }

    private boolean esGuardia() {
        return securityContext != null && securityContext.isUserInRole("GUARDIA");
    }

    private boolean esVisitante() {
        return securityContext != null && securityContext.isUserInRole("VISITANTE");
    }

    private boolean esPropietarioVisita(Visita visita, String username) {
        if (visita == null || username == null || username.trim().isEmpty()) {
            return false;
        }

        String ownerUsername = visita.getVisitante() != null
                && visita.getVisitante().getUsuario() != null
                ? visita.getVisitante().getUsuario().getUsername()
                : null;

        return ownerUsername != null && ownerUsername.equalsIgnoreCase(username.trim());
    }

    private Visita buscarVisitaConRelaciones(Integer id) {
        return em.createQuery(
                "SELECT v FROM Visita v "
                + "LEFT JOIN FETCH v.reo "
                + "LEFT JOIN FETCH v.visitante "
                + "LEFT JOIN FETCH v.visitante.usuario "
                + "WHERE v.id = :id", Visita.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @GET
    @Path("/mis-citas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMisCitas() {
        if (securityContext == null || !securityContext.isUserInRole("VISITANTE")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Acceso denegado"))
                    .build();
        }

        String username = securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName()
                : null;

        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Usuario no identificado"))
                    .build();
        }

        List<Visita> citas = em.createQuery(
                "SELECT DISTINCT v FROM Visita v "
                + "LEFT JOIN FETCH v.reo "
                + "LEFT JOIN FETCH v.visitante "
                + "LEFT JOIN FETCH v.visitante.usuario "
                + "JOIN v.visitante vis "
                + "JOIN vis.usuario u "
                + "WHERE u.username = :username "
                + "ORDER BY v.fechaVisita DESC, v.id DESC",
                Visita.class)
                .setParameter("username", username.trim())
                .getResultList();

        LOG.info("GET /visitas/mis-citas para username=" + username + ": " + citas.size() + " citas");
        return Response.ok(citas).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllVisitas() {
        if (securityContext == null
                || (!securityContext.isUserInRole("ADMIN") && !securityContext.isUserInRole("GUARDIA"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado"))
                    .build();
        }

        List<Visita> visitas = em.createQuery(
                "SELECT DISTINCT v FROM Visita v "
                + "LEFT JOIN FETCH v.reo "
                + "LEFT JOIN FETCH v.visitante "
                + "LEFT JOIN FETCH v.visitante.usuario "
                + "ORDER BY v.fechaVisita DESC, v.id DESC",
                Visita.class)
                .getResultList();

        LOG.info("GET /visitas -> " + visitas.size() + " visitas");
        return Response.ok(visitas).build();
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"ADMIN", "GUARDIA", "VISITANTE"})
    public Response getById(@PathParam("id") Integer id) {
        Visita v = buscarVisitaConRelaciones(id);
        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Blindaje: si es visitante, debe ser su visita
        if (esVisitante()) {
            if (!esPropietarioVisita(v, getUsername())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "No tienes acceso a esta visita"))
                        .build();
            }
        }
        return Response.ok(v).build();
    }

    @POST
    @Transactional
    @RolesAllowed("VISITANTE")
    public Response create(CrearVisitaRequest request) {
        LOG.info("POST /visitas");

        String username = getUsername();

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
            nuevaVisita.setValidada(false);
            nuevaVisita.setFechaValidacion(null);

            em.persist(nuevaVisita);
            em.flush();

            LOG.info("Visita creada. id=" + nuevaVisita.getId() + ", username=" + username);

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
    @RolesAllowed({"ADMIN", "VISITANTE"})
    public Response update(@PathParam("id") Integer id, Visita visitaUpdate) {
        Visita v = buscarVisitaConRelaciones(id);

        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe una visita con id: " + id))
                    .build();
        }

        String username = getUsername();

        if (esVisitante() && !esPropietarioVisita(v, username)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No puedes modificar una visita que no es tuya"))
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

            if (visitaUpdate.getReo() != null && visitaUpdate.getReo().getId() != null) {
                Reo reo = em.find(Reo.class, visitaUpdate.getReo().getId());
                if (reo == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("error", "No existe un reo con id: " + visitaUpdate.getReo().getId()))
                            .build();
                }
                v.setReo(reo);
            }

            if (esAdmin()) {
                if (visitaUpdate.getAutorizado() != null) {
                    v.setAutorizado(visitaUpdate.getAutorizado());
                }

                if (visitaUpdate.getVisitante() != null && visitaUpdate.getVisitante().getId() != null) {
                    Visitante visitante = em.find(Visitante.class, visitaUpdate.getVisitante().getId());
                    if (visitante == null) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(Map.of("error", "No existe un visitante con id: " + visitaUpdate.getVisitante().getId()))
                                .build();
                    }
                    v.setVisitante(visitante);
                }

                if (visitaUpdate.getCodigoQr() != null && !visitaUpdate.getCodigoQr().trim().isEmpty()) {
                    v.setCodigoQr(visitaUpdate.getCodigoQr().trim());
                }
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

    @PATCH
    @Path("{id}/autorizar")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response autorizar(@PathParam("id") Integer id) {
        Visita v = em.find(Visita.class, id);
        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        v.setAutorizado(true);
        // IMPORTANTE: aquí podrías disparar notificaciones o lógica extra
        em.merge(v);
        return Response.ok(Map.of("mensaje", "Visita autorizada")).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Integer id) {
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
    @RolesAllowed("VISITANTE")
    public Response generarQR(@PathParam("id") Integer id) {
        Visita v = buscarVisitaConRelaciones(id);
        if (v == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Validar propiedad
        if (!esPropietarioVisita(v, getUsername())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // Validar estado de negocio
        if (!Boolean.TRUE.equals(v.getAutorizado())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Visita no autorizada"))
                    .build();
        }

        String qrCode = "VISITA_" + UUID.randomUUID().toString().substring(0, 8);
        v.setCodigoQr(qrCode);
        v.setValidada(false);
        em.merge(v);

        return Response.ok(Map.of("qr", qrCode)).build();
    }

    @POST
    @Path("validar-qr")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional

    public Response validarQR(@FormParam("qr") String qrCode) {

        // 1. Verificación de Seguridad: Solo GUARDIA o ADMIN pueden validar
        if (securityContext == null
                || (!securityContext.isUserInRole("GUARDIA") && !securityContext.isUserInRole("ADMIN"))) {

            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of(
                            "valido", false,
                            "mensaje", "No tiene permisos para realizar esta acción"
                    ))
                    .build();
        }

        // 2. Validación de entrada
        if (qrCode == null || qrCode.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "valido", false,
                            "mensaje", "Código QR no proporcionado"
                    ))
                    .build();
        }

        // 3. Búsqueda de la visita y su visitante
        List<Visita> visitas = em.createQuery(
                "SELECT v FROM Visita v "
                + "LEFT JOIN FETCH v.visitante "
                + "WHERE v.codigoQr = :qr", Visita.class)
                .setParameter("qr", qrCode.trim())
                .getResultList();

        // 4. Caso: El QR no existe en la base de datos
        if (visitas.isEmpty()) {
            return Response.ok(Map.of(
                    "valido", false,
                    "mensaje", "Código QR no reconocido"
            )).build();
        }

        Visita v = visitas.get(0);

        // 5. Caso: La visita existe pero no ha sido aprobada por el Admin
        if (!Boolean.TRUE.equals(v.getAutorizado())) {
            return Response.ok(Map.of(
                    "valido", false,
                    "mensaje", "Acceso denegado: Visita pendiente de autorización"
            )).build();
        }

        // 6. Caso: El QR es válido pero ya se escaneó anteriormente (Seguridad contra duplicados)
        if (Boolean.TRUE.equals(v.getValidada())) {
            return Response.ok(Map.of(
                    "valido", false,
                    "mensaje", "Acceso denegado: Este código ya ha sido utilizado"
            )).build();
        }

        // 7. ÉXITO: Marcamos la entrada y guardamos cambios
        v.setValidada(true);
        v.setFechaValidacion(LocalDateTime.now()); // Registro de la hora exacta de entrada

        try {
            em.merge(v);
            em.flush();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("valido", false, "mensaje", "Error al registrar el acceso"))
                    .build();
        }

        // Enviamos respuesta positiva con el nombre del visitante para el guardia
        return Response.ok(Map.of(
                "valido", true,
                "visitante", v.getVisitante() != null ? v.getVisitante().getNombreCompleto() : "Invitado",
                "mensaje", "Acceso concedido. Puede ingresar."
        )).build();
    }
}
