package com.jongsoft.finance.jackson;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;

@MicronautTest
@DisplayName("Serializer for LocalDateTime")
class LocalDateTimeSerializerTest {

    @Test
    @DisplayName("Should serialize LocalDateTime to ISO format")
    void serialize(ObjectMapper objectMapper) throws IOException {
        var serialized = objectMapper.writeValueAsString(LocalDateTime.of(2019, 2, 1, 12, 12));
        Assertions.assertThat(serialized).isEqualTo("\"2019-02-01T12:12:00\"");
    }

    @Test
    @DisplayName("Should deserialize LocalDateTime from ISO format")
    void deserialize(ObjectMapper objectMapper) throws IOException {
        var deserialized = objectMapper.readValue("\"2019-02-01T12:12:00\"", LocalDateTime.class);
        Assertions.assertThat(deserialized).isEqualTo(LocalDateTime.of(2019, 2, 1, 12, 12));
    }
}