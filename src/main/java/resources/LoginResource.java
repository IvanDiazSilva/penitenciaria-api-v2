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
import jwt.JwtUtil;
import model.Usuario;

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
                    .entity("{\"mensaje\":\"Faltan usuario o contraseña\"}")
                    .build();
        }

        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.username = :user AND u.password = :pass",
                    Usuario.class
            );

            query.setParameter("user", request.getUsername().trim());
            query.setParameter("pass", request.getPassword().trim());

            Usuario usuario = query.getSingleResult();

            String token = JwtUtil.generarToken(usuario);

            String json = String.format(
                    "{\"mensaje\":\"login correcto\",\"token\":\"%s\",\"rol\":\"%s\",\"username\":\"%s\"}",
                    token,
                    usuario.getRol(),
                    usuario.getUsername()
            );

            return Response.ok(json).build();

        } catch (NoResultException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"mensaje\":\"Credenciales incorrectas\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"mensaje\":\"Error interno en login\"}")
                    .build();
        }
    }
}