package jwt;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        if (esRutaPublica(path)) {
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

        request.setAttribute("username", username);
        request.setAttribute("rol", rol);
    }

    private boolean esRutaPublica(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        String ruta = path.trim().toLowerCase();

        return ruta.equals("login")
                || ruta.endsWith("/login")
                || ruta.equals("visitantes/preregistro")
                || ruta.endsWith("/visitantes/preregistro")
                || ruta.startsWith("visitantes/estado/")
                || ruta.contains("/visitantes/estado/");
    }
}
