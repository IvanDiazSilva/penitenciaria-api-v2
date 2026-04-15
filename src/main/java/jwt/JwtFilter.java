package jwt;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        if ("login".equals(path) || path.endsWith("/login")
                || "visitantes/preregistro".equals(path)
                || path.endsWith("/visitantes/preregistro")) {
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .type(MediaType.APPLICATION_JSON)
                            .entity("{\"error\":\"Falta cabecera Authorization\"}")
                            .build()
            );
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        if (!JwtUtil.validarToken(token)) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .type(MediaType.APPLICATION_JSON)
                            .entity("{\"error\":\"Token inválido\"}")
                            .build()
            );
            return;
        }

        String username = JwtUtil.getUsername(token);
        String rol = JwtUtil.getRol(token);

        System.out.println("JWT username = " + username);
        System.out.println("JWT rol = " + rol);

        requestContext.setProperty("username", username);
        requestContext.setProperty("rol", rol);
    }
}
