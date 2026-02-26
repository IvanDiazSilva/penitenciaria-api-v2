package com.ivan.penitenciaria.api;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;

@Provider
@ApplicationScoped
public class JsonConfig implements ContextResolver<ObjectMapper> {
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    public JsonConfig() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
