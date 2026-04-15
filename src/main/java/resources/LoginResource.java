package resources;

import dto.LoginRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import jwt.JwtUtil;
import model.Usuario;
import model.Visitante;

@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource {

    @PersistenceContext(unitName = "PenitenciariaPU")
    private EntityManager em;

    @POST
    @Transactional
    public Response login(LoginRequest request) {
        if (request == null
                || request.getUsername() == null || request.getUsername().trim().isEmpty()
                || request.getPassword() == null || request.getPassword().trim().isEmpty()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("mensaje", "Faltan usuario o contraseña"))
                    .build();
        }

        try {
            String username = request.getUsername().trim();
            String password = request.getPassword().trim();

            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.username = :user",
                    Usuario.class
            );
            query.setParameter("user", username);

            Usuario usuario = query.getSingleResult();

            if (!usuario.getPassword().equals(password)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("mensaje", "Credenciales incorrectas"))
                        .build();
            }

            if ("VISITANTE".equalsIgnoreCase(usuario.getRol())) {
                Visitante visitante = usuario.getVisitante();

                if (visitante == null) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(Map.of("mensaje", "El usuario visitante no está vinculado a un visitante"))
                            .build();
                }

                if (!"APROBADO".equalsIgnoreCase(visitante.getEstado())) {
                    return Response.status(Response.Status.FORBIDDEN)
                            .entity(Map.of(
                                    "mensaje", "Tu solicitud aún no está aprobada",
                                    "estado", visitante.getEstado()
                            ))
                            .build();
                }
            }

            String token = JwtUtil.generarToken(usuario);

            return Response.ok(Map.of(
                    "mensaje", "login correcto",
                    "token", token,
                    "rol", usuario.getRol(),
                    "username", usuario.getUsername()
            )).build();

        } catch (NoResultException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("mensaje", "Credenciales incorrectas"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("mensaje", "Error interno en login"))
                    .build();
        }
    }
}