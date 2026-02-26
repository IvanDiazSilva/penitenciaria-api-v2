package com.ivan.penitenciaria.api;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
                || request.getUsername() == null
                || request.getPassword() == null) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"mensaje\":\"Faltan usuario o contraseña\"}")
                    .build();
        }

        TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.username = :user AND u.password = :pass",
                Usuario.class
        );
        query.setParameter("user", request.getUsername());
        query.setParameter("pass", request.getPassword());

        Usuario usuario;
        try {
            usuario = query.getSingleResult();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"mensaje\":\"Credenciales incorrectas\"}")
                    .build();
        }

        // Generar token JWT
        String token = JwtUtil.generarToken(usuario);

        String json = String.format(
                "{\"mensaje\":\"login correcto\",\"token\":\"%s\",\"rol\":\"%s\"}",
                token,
                usuario.getRol()
        );

        return Response.ok(json).build();
    }

}
