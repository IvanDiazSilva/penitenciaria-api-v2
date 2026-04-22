package resources;

import model.Reo;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/reos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ReoResource {

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    @GET
    public List<Reo> getAllReos() {
        return em.createQuery("SELECT r FROM Reo r ORDER BY r.id", Reo.class).getResultList();
    }

    @GET
    @Path("/{id}")
    public Response getReo(@PathParam("id") Integer id) {
        Reo reo = em.find(Reo.class, id);

        if (reo == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "No existe un recluso con ese id");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        return Response.ok(reo).build();
    }

    @POST
    @Transactional
    public Response crearReo(Reo reo, @Context HttpServletRequest req) {
        if (!autorizadoReos(req)) {
            return forbidden();
        }

        if (reo == null || esVacio(reo.getNombre()) || esVacio(reo.getDni()) || esVacio(reo.getDelito())) {
            return badRequest("Nombre, DNI y delito son obligatorios");
        }

        em.persist(reo);
        em.flush();

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Recluso creado correctamente");
        response.put("reo", reo);

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response actualizarReo(@PathParam("id") Integer id, Reo reoUpdate, @Context HttpServletRequest req) {
        if (!autorizadoReos(req)) {
            return forbidden();
        }

        Reo reo = em.find(Reo.class, id);
        if (reo == null) {
            return notFound("No existe un recluso con ese id");
        }

        if (reoUpdate == null || esVacio(reoUpdate.getNombre()) || esVacio(reoUpdate.getDni()) || esVacio(reoUpdate.getDelito())) {
            return badRequest("Nombre, DNI y delito son obligatorios");
        }

        reo.setNombre(reoUpdate.getNombre());
        reo.setDni(reoUpdate.getDni());
        reo.setDelito(reoUpdate.getDelito());

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Recluso actualizado correctamente");
        response.put("reo", reo);

        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response eliminarReo(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        if (!autorizadoReos(req)) {
            return forbidden();
        }

        Reo reo = em.find(Reo.class, id);
        if (reo == null) {
            return notFound("No existe un recluso con ese id");
        }

        try {
            em.remove(reo);
            em.flush();

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Recluso eliminado correctamente");
            response.put("id", id);

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("mensaje", "No se puede eliminar el recluso porque tiene registros asociados"))
                    .build();
        }
    }

    private boolean autorizadoReos(HttpServletRequest req) {
        String rol = (String) req.getAttribute("rol");
        return rol != null && rol.equalsIgnoreCase("ADMIN");
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private Response forbidden() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("mensaje", "No autorizado"))
                .build();
    }

    private Response notFound(String mensaje) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("mensaje", mensaje))
                .build();
    }

    private Response badRequest(String mensaje) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("mensaje", mensaje))
                .build();
    }
}
