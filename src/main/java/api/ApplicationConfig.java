package api;

import cors.CorsFilter;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import jwt.JwtFilter;
import resources.IncidenteResource;
import resources.LoginResource;
import resources.MonitorResource;
import resources.ReoResource;
import resources.VisitaResource;
import resources.VisitanteResource;

@ApplicationPath("/api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(LoginResource.class);
        classes.add(ReoResource.class);
        classes.add(VisitaResource.class);
        classes.add(IncidenteResource.class);
        classes.add(VisitanteResource.class);
        classes.add(MonitorResource.class);
        classes.add(JwtFilter.class);
        classes.add(CorsFilter.class);

        return classes;
    }
}