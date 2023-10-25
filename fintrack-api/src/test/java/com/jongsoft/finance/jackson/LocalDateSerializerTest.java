package com.jongsoft.finance.jackson;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

@MicronautTest
class LocalDateSerializerTest {

    @Test
    void serialize(ObjectMapper objectMapper) throws IOException {
        var json = objectMapper.writeValueAsString(LocalDate.of(2019, 2, 1));
        Assertions.assertThat(json).isEqualTo("\"2019-02-01\"");
    }

    @Test
    void deserialize(ObjectMapper objectMapper) throws IOException {
        var localDate = objectMapper.readValue("\"2019-02-01\"", LocalDate.class);
        Assertions.assertThat(localDate).isEqualTo(LocalDate.of(2019, 2, 1));
    }
}