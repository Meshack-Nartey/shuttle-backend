package com.shuttlebackend.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = false)
public class PolylineJsonConverter implements AttributeConverter<List<List<Double>>, String> {

    private static final TypeReference<List<List<Double>>> TYPE_REF =
            new TypeReference<List<List<Double>>>() {};

    @Override
    public String convertToDatabaseColumn(List<List<Double>> attribute) {
        try {
            if (attribute == null || attribute.isEmpty()) {
                return "[]"; // NEVER return null
            }
            return JsonConverterUtils.mapper().writeValueAsString(attribute);
        } catch (Exception ex) {
            System.err.println("PolylineJsonConverter: failed to write JSON: " + ex.getMessage());
            return "[]"; // safe fallback
        }
    }

    @Override
    public List<List<Double>> convertToEntityAttribute(String dbData) {
        try {
            return JsonConverterUtils.safeRead(dbData, TYPE_REF)
                    .orElseGet(ArrayList::new); // NEVER return null
        } catch (Exception ex) {
            System.err.println("PolylineJsonConverter: unexpected error: " + ex.getMessage());
            return new ArrayList<>();
        }
    }
}
