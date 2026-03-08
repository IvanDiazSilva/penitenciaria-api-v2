package jwt;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    @Context
    HttpServletRequest req;  // ← AÑADIDO para setAttribute

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        System.out.println("PATH = " + path);

        if (path.equals("/login")) {
            return;  // Login libre
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ FALTA HEADER");
            requestContext.abortWith(Response.status(401)
                    .entity("{\"error\":\"Falta cabecera Authorization\"}").build());
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (!JwtUtil.validarToken(token)) {
            System.out.println("❌ TOKEN INVÁLIDO");
            requestContext.abortWith(Response.status(401)
                    .entity("{\"error\":\"Token inválido\"}").build());
            return;
        }

        // ← AÑADIDO: SET ROL para IncidenteResource
        String username = JwtUtil.getUsername(token);
        String rol = JwtUtil.getRol(token);
        System.out.println("✅ TOKEN OK - " + username + " rol=[" + rol + "]");
        req.setAttribute("username", username);
        req.setAttribute("rol", rol);  // ← CRÍTICO

        System.out.println("✅ PASANDO A RESOURCE rol=" + rol);
    }
}
