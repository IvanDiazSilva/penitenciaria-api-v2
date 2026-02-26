package com.ivan.penitenciaria.api;

import java.util.HashSet;
import java.util.Set;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(LoginResource.class);
        classes.add(ReoResource.class);
        classes.add(JwtFilter.class);  // ← ¡LO QUE FALTABA!
        classes.add(VisitaResource.class);
        classes.add(JsonConfig.class);


        return classes;
    }
}
