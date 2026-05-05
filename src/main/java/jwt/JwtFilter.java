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
import jakarta.ws.rs.core.SecurityContext;
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
            abortar(requestContext, Response.Status.UNAUTHORIZED, "Falta cabecera Authorization");
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        if (token.isEmpty()) {
            abortar(requestContext, Response.Status.UNAUTHORIZED, "Token vacío");
            return;
        }

        if (!JwtUtil.validarToken(token)) {
            abortar(requestContext, Response.Status.UNAUTHORIZED, "Token inválido");
            return;
        }

        String username = JwtUtil.getUsername(token);
        String rol = JwtUtil.getRol(token);

        if (username == null || username.trim().isEmpty()) {
            abortar(requestContext, Response.Status.UNAUTHORIZED, "Token sin username válido");
            return;
        }

        if (rol == null || rol.trim().isEmpty()) {
            abortar(requestContext, Response.Status.UNAUTHORIZED, "Token sin rol válido");
            return;
        }

        final SecurityContext currentSecurityContext = requestContext.getSecurityContext();

        JwtSecurityContext jwtSecurityContext = new JwtSecurityContext(
                username.trim(),
                rol.trim(),
                currentSecurityContext != null && currentSecurityContext.isSecure()
        );

        requestContext.setSecurityContext(jwtSecurityContext);

        // Compatibilidad temporal con código antiguo
        requestContext.setProperty("username", username.trim());
        requestContext.setProperty("rol", rol.trim());

        request.setAttribute("username", username.trim());
        request.setAttribute("rol", rol.trim());
    }

    private void abortar(ContainerRequestContext requestContext, Response.Status status, String mensaje) {
        requestContext.abortWith(
                Response.status(status)
                        .type(MediaType.APPLICATION_JSON)
                        .entity("{\"error\":\"" + escaparJson(mensaje) + "\"}")
                        .build()
        );
    }

    private String escaparJson(String texto) {
        return texto == null ? "" : texto.replace("\"", "\\\"");
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