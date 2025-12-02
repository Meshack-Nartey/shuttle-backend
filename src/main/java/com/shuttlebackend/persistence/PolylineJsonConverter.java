package com.shuttlebackend.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter(autoApply = false)
public class PolylineJsonConverter implements AttributeConverter<List<List<Double>>, String> {
    private static final ObjectMapper M = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<List<Double>> attribute) {
        try {
            if (attribute == null) return null;
            return M.writeValueAsString(attribute);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<List<Double>> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) return Collections.emptyList();
            return M.readValue(dbData, new TypeReference<List<List<Double>>>(){});
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

