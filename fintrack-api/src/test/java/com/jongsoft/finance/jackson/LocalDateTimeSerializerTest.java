package com.jongsoft.finance.jackson;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;

@MicronautTest
class LocalDateTimeSerializerTest {

    @Test
    void serialize(ObjectMapper objectMapper) throws IOException {
        var serialized = objectMapper.writeValueAsString(LocalDateTime.of(2019, 2, 1, 12, 12));
        Assertions.assertThat(serialized).isEqualTo("\"2019-02-01T12:12:00\"");
    }

    @Test
    void deserialize(ObjectMapper objectMapper) throws IOException {
        var deserialized = objectMapper.readValue("\"2019-02-01T12:12:00\"", LocalDateTime.class);
        Assertions.assertThat(deserialized).isEqualTo(LocalDateTime.of(2019, 2, 1, 12, 12));
    }
}