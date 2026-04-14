package resources;

import model.Visitante;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/visitantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VisitanteResource {

    private static final Logger LOG = Logger.getLogger(VisitanteResource.class.getName());

    @Context
    private HttpServletRequest req;

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    private boolean tieneRol(String... rolesPermitidos) {
        String rol = (String) req.getAttribute("rol");
        LOG.info("ROL LEIDO EN VisitanteResource = '" + rol + "'");

        if (rol == null) {
            return false;
        }

        for (String permitido : rolesPermitidos) {
            if (rol.equalsIgnoreCase(permitido)) {
                return true;
            }
        }

        return false;
    }

    @POST
    @Transactional
    public Response create(Visitante visitante) {
        try {
            if (visitante == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No se han recibido datos del visitante"))
                        .build();
            }

            if (visitante.getNombreCompleto() == null || visitante.getNombreCompleto().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El nombre completo es obligatorio"))
                        .build();
            }

            if (visitante.getDniNie() == null || visitante.getDniNie().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El DNI/NIE es obligatorio"))
                        .build();
            }

            visitante.setNombreCompleto(visitante.getNombreCompleto().trim());
            visitante.setDniNie(visitante.getDniNie().trim().toUpperCase());

            if (visitante.getNacionalidad() != null) {
                visitante.setNacionalidad(visitante.getNacionalidad().trim());
            }

            if (visitante.getTelefono() != null) {
                visitante.setTelefono(visitante.getTelefono().trim());
            }

            if (visitante.getEmail() != null) {
                visitante.setEmail(visitante.getEmail().trim());
            }

            if (visitante.getDireccion() != null) {
                visitante.setDireccion(visitante.getDireccion().trim());
            }

            if (visitante.getNombreInterno() != null) {
                visitante.setNombreInterno(visitante.getNombreInterno().trim());
            }

            if (visitante.getParentesco() != null) {
                visitante.setParentesco(visitante.getParentesco().trim());
            }

            if (visitante.getEstado() == null || visitante.getEstado().trim().isEmpty()) {
                visitante.setEstado("PENDIENTE");
            } else {
                visitante.setEstado(visitante.getEstado().trim().toUpperCase());
            }

            Long existentes = em.createQuery(
                    "SELECT COUNT(v) FROM Visitante v WHERE UPPER(v.dniNie) = :dniNie",
                    Long.class
            ).setParameter("dniNie", visitante.getDniNie())
                    .getSingleResult();

            if (existentes > 0) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("error", "Ya existe un visitante con ese DNI/NIE"))
                        .build();
            }

            em.persist(visitante);
            em.flush();
            em.refresh(visitante);

            LOG.info("Visitante creado con id=" + visitante.getId() + ", dniNie=" + visitante.getDniNie());

            return Response.status(Response.Status.CREATED)
                    .entity(visitante)
                    .build();

        } catch (Exception e) {
            LOG.severe("Error POST visitante: " + e.getClass().getName() + " - " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno al crear el visitante"))
                    .build();
        }
    }

    @GET
    public Response getAll() {
        if (!tieneRol("ADMIN", "GUARDIA")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado"))
                    .build();
        }

        List<Visitante> visitantes = em.createQuery(
                "SELECT v FROM Visitante v ORDER BY v.fechaCreacion DESC",
                Visitante.class
        ).getResultList();

        LOG.info("GET /visitantes -> " + visitantes.size() + " visitantes");

        return Response.ok(visitantes).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") Long id) {
        if (!tieneRol("ADMIN", "GUARDIA")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado"))
                    .build();
        }

        Visitante visitante = em.find(Visitante.class, id);

        if (visitante == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe un visitante con id: " + id))
                    .build();
        }

        return Response.ok(visitante).build();
    }

    @GET
    @Path("/estado/{dni}")
    public Response consultarEstadoPorDni(@PathParam("dni") String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El DNI/NIE es obligatorio"))
                    .build();
        }

        String dniNormalizado = dni.trim().toUpperCase();

        try {
            Visitante visitante = em.createQuery(
                    "SELECT v FROM Visitante v WHERE UPPER(v.dniNie) = :dniNie",
                    Visitante.class
            ).setParameter("dniNie", dniNormalizado)
                    .getSingleResult();

            LOG.info("GET /visitantes/estado/" + dniNormalizado + " -> " + visitante.getEstado());

            return Response.ok(Map.of(
                    "dniNie", visitante.getDniNie(),
                    "estado", visitante.getEstado(),
                    "nombreCompleto", visitante.getNombreCompleto()
            )).build();

        } catch (NoResultException e) {
            LOG.info("GET /visitantes/estado/" + dniNormalizado + " -> NO ENCONTRADO");

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No se encontró ninguna solicitud con ese DNI/NIE"))
                    .build();
        }
    }

    @PUT
    @Path("{id}/estado")
    @Transactional
    public Response actualizarEstado(@PathParam("id") Long id, Map<String, String> body) {
        if (!tieneRol("ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "No autorizado"))
                    .build();
        }

        Visitante visitante = em.find(Visitante.class, id);
        if (visitante == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No existe un visitante con id: " + id))
                    .build();
        }

        if (body == null || body.get("estado") == null || body.get("estado").trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El campo estado es obligatorio"))
                    .build();
        }

        String nuevoEstado = body.get("estado").trim().toUpperCase();

        if (!nuevoEstado.equals("PENDIENTE")
                && !nuevoEstado.equals("APROBADO")
                && !nuevoEstado.equals("RECHAZADO")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Estado no válido. Usa PENDIENTE, APROBADO o RECHAZADO"))
                    .build();
        }

        visitante.setEstado(nuevoEstado);
        em.merge(visitante);
        em.flush();

        LOG.info("Estado visitante id=" + id + " actualizado a " + nuevoEstado);

        return Response.ok(Map.of(
                "mensaje", "Estado actualizado correctamente",
                "id", visitante.getId(),
                "estado", visitante.getEstado()
        )).build();
    }
}
