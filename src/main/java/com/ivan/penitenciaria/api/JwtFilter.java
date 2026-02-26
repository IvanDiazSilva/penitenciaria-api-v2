package com.ivan.penitenciaria.api;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        System.out.println("PATH = " + path);

        // PROTEGER solo reos + visitas (path SIN /api/)
        if (!path.equals("/reos") && !path.equals("/visitas")) {
            return;  // /login libre
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
        }

        System.out.println("✅ TOKEN OK");
    }

}
