package com.shuttlebackend.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();

        // Register Java 8 date/time module
        om.registerModule(new JavaTimeModule());

        // Use ISO-8601 for dates (not timestamps)
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Ignore unknown properties to be tolerant for incoming JSON
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Don't serialize nulls (keeps payloads smaller)
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Other safe defaults can be enabled/disabled here as needed
        return om;
    }
}

