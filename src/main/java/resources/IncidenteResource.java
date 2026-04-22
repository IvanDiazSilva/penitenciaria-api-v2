package resources;

import model.Incidente;
import model.Reo;
import model.Usuario;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/incidentes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IncidenteResource {

    private static final Logger LOG = Logger.getLogger(IncidenteResource.class.getName());

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    private boolean autorizadoIncidentes(@Context HttpServletRequest req) {
        String rol = (String) req.getAttribute("rol");
        return rol != null && (rol.equalsIgnoreCase("ADMIN")
                || rol.equalsIgnoreCase("GUARDIA"));
    }

    @GET
    public Response getAll(@Context HttpServletRequest req) {
        if (!autorizadoIncidentes(req)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Rol ADMIN/GUARDIA requerido"))
                    .build();
        }

        List<Incidente> incidentes = em.createQuery(
                "SELECT i FROM Incidente i " +
                "LEFT JOIN FETCH i.guardia " +
                "LEFT JOIN FETCH i.reo " +
                "ORDER BY i.fechaHora DESC",
                Incidente.class)
                .setMaxResults(100)
                .getResultList();

        LOG.info("GET /api/incidentes (" + incidentes.size() + ")");
        return Response.ok(incidentes).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        if (!autorizadoIncidentes(req)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado"))
                    .build();
        }

        Incidente incidente = em.find(Incidente.class, id);

        if (incidente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Incidente no encontrado"))
                    .build();
        }

        return Response.ok(incidente).build();
    }

    @POST
    @Transactional
    public Response create(Incidente incidente, @Context HttpServletRequest req) {
        if (!autorizadoIncidentes(req)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado crear incidentes"))
                    .build();
        }

        LOG.info("POST /api/incidentes: " + incidente);

        try {
            if (incidente == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No se han recibido datos del incidente"))
                        .build();
            }

            if (incidente.getTipo() == null || incidente.getTipo().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El tipo es obligatorio"))
                        .build();
            }

            if (incidente.getDescripcion() == null || incidente.getDescripcion().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "La descripcion es obligatoria"))
                        .build();
            }

            if (incidente.getGuardia() == null || incidente.getGuardia().getId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El guardia es obligatorio"))
                        .build();
            }

            if (incidente.getFechaHora() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "La fechaHora es obligatoria"))
                        .build();
            }

            Usuario guardia = em.find(Usuario.class, incidente.getGuardia().getId());
            if (guardia == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No existe un usuario/guardia con id: " + incidente.getGuardia().getId()))
                        .build();
            }

            incidente.setTipo(incidente.getTipo().trim());
            incidente.setDescripcion(incidente.getDescripcion().trim());
            incidente.setGuardia(guardia);

            if (incidente.getReo() != null && incidente.getReo().getId() != null) {
                Reo reo = em.find(Reo.class, incidente.getReo().getId());

                if (reo == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("error", "No existe un reo con id: " + incidente.getReo().getId()))
                            .build();
                }

                incidente.setReo(reo);
            } else {
                incidente.setReo(null);
            }

            em.persist(incidente);
            em.flush();

            return Response.status(Response.Status.CREATED).entity(incidente).build();

        } catch (Exception e) {
            LOG.severe("Error POST incidente: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Error crear incidente: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response update(@PathParam("id") Integer id, Incidente update, @Context HttpServletRequest req) {
        if (!autorizadoIncidentes(req)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado"))
                    .build();
        }

        Incidente incidente = em.find(Incidente.class, id);
        if (incidente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Incidente no encontrado"))
                    .build();
        }

        try {
            if (update == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Los datos del incidente son obligatorios"))
                        .build();
            }

            if (update.getTipo() != null && !update.getTipo().trim().isEmpty()) {
                incidente.setTipo(update.getTipo().trim());
            }

            if (update.getDescripcion() != null && !update.getDescripcion().trim().isEmpty()) {
                incidente.setDescripcion(update.getDescripcion().trim());
            }

            if (update.getFechaHora() != null) {
                incidente.setFechaHora(update.getFechaHora());
            }

            if (update.getGuardia() != null && update.getGuardia().getId() != null) {
                Usuario guardia = em.find(Usuario.class, update.getGuardia().getId());

                if (guardia == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("error", "No existe un guardia con id: " + update.getGuardia().getId()))
                            .build();
                }

                incidente.setGuardia(guardia);
            }

            if (update.getReo() != null) {
                if (update.getReo().getId() != null) {
                    Reo reo = em.find(Reo.class, update.getReo().getId());

                    if (reo == null) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(Map.of("error", "No existe un reo con id: " + update.getReo().getId()))
                                .build();
                    }

                    incidente.setReo(reo);
                } else {
                    incidente.setReo(null);
                }
            }

            em.merge(incidente);
            em.flush();

            return Response.ok(incidente).build();

        } catch (Exception e) {
            LOG.severe("Error PUT incidente: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Error actualizar incidente: " + e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        if (!autorizadoIncidentes(req)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado para eliminar incidentes"))
                    .build();
        }

        Incidente incidente = em.find(Incidente.class, id);
        if (incidente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Incidente no encontrado"))
                    .build();
        }

        em.remove(incidente);

        return Response.ok(Map.of(
                "mensaje", "Incidente eliminado correctamente",
                "id", id
        )).build();
    }
}