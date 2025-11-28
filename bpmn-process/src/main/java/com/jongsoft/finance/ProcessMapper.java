package com.jongsoft.finance;

import com.jongsoft.lang.Control;
import com.jongsoft.lang.control.Try;

import io.micronaut.serde.ObjectMapper;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ProcessMapper {

    private final ObjectMapper objectMapper;

    public ProcessMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> String writeSafe(T entity) {
        return Control.Try(() -> objectMapper.writeValueAsString(entity))
                .recover(x -> {
                    log.warn("Could not serialize entity {}", entity, x);
                    return null;
                })
                .get();
    }

    public <T> T readSafe(String json, Class<T> clazz) {
        return Control.Try(() -> objectMapper.readValue(json, clazz))
                .recover(x -> {
                    log.warn("Could not deserialize json {}", json, x);
                    return null;
                })
                .get();
    }

    public <T> Try<T> read(String json, Class<T> clazz) {
        return Control.Try(() -> objectMapper.readValue(json, clazz));
    }
}
