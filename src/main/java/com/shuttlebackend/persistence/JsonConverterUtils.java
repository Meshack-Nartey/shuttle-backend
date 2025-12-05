// New file: safe JSON parsing utilities for AttributeConverters
package com.shuttlebackend.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class JsonConverterUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonConverterUtils.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonConverterUtils() { /* utility */ }

    public static ObjectMapper mapper() { return MAPPER; }

    /**
     * Safely parse JSON string to a target type by using a Class<T> target.
     */
    public static <T> Optional<T> safeRead(String json, Class<T> clazz) {
        if (json == null) return Optional.empty();
        if (json.isBlank()) return Optional.empty();
        try {
            T val = MAPPER.readValue(json, clazz);
            return Optional.ofNullable(val);
        } catch (Exception ex) {
            logger.warn("JsonConverterUtils: failed to parse JSON into {}: {}", clazz.getSimpleName(), ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Safely parse JSON string using a TypeReference (for generic types like List<List<Double>>).
     */
    public static <T> Optional<T> safeRead(String json, TypeReference<T> typeRef) {
        if (json == null) return Optional.empty();
        if (json.isBlank()) return Optional.empty();
        try {
            T val = MAPPER.readValue(json, typeRef);
            return Optional.ofNullable(val);
        } catch (Exception ex) {
            logger.warn("JsonConverterUtils: failed to parse JSON into {}: {}", typeRef.getType(), ex.getMessage());
            return Optional.empty();
        }
    }

}
