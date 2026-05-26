package com.slt.iau_portal.util;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ListToJsonConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            if (attribute == null) return null;
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to convert list to JSON", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) return new ArrayList<>();
            try {
                return mapper.readValue(dbData, new TypeReference<List<String>>(){});
            } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException mie) {
                // Some DBs may store a JSON array as a quoted string (e.g. '"[]"').
                // Try to unescape the inner string and parse again.
                try {
                    String inner = mapper.readValue(dbData, String.class);
                    if (inner == null || inner.isBlank()) return new ArrayList<>();
                    return mapper.readValue(inner, new TypeReference<List<String>>(){});
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to convert JSON to list", ex);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to convert JSON to list", e);
        }
    }
}
