package cors;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    private static final String ALLOWED_ORIGIN = "http://localhost:4200";

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) {
        resp.getHeaders().putSingle("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        resp.getHeaders().putSingle("Access-Control-Allow-Headers",
                "Origin, Content-Type, Accept, Authorization, X-Requested-With");
        resp.getHeaders().putSingle("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}