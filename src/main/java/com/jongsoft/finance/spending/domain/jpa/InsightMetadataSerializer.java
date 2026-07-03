package com.jongsoft.finance.spending.domain.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.Collection;
import java.util.Map;

/** Serializes insight/pattern metadata values for JPA string storage. */
public final class InsightMetadataSerializer {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private InsightMetadataSerializer() {}

    public static String serialize(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection<?> || value instanceof Map<?, ?>) {
            try {
                return MAPPER.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                return value.toString();
            }
        }
        return value.toString();
    }
}
