package com.jongsoft.finance.jackson;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

@DisplayName("LocalDate serializer")
@MicronautTest(environments = {"no-camunda", "no-analytics"} )
class LocalDateSerializerTest {

    @Test
    @DisplayName("Serialize LocalDate")
    void serialize(ObjectMapper objectMapper) throws IOException {
        var json = objectMapper.writeValueAsString(LocalDate.of(2019, 2, 1));
        Assertions.assertThat(json).isEqualTo("\"2019-02-01\"");
    }

    @Test
    @DisplayName("Deserialize LocalDate")
    void deserialize(ObjectMapper objectMapper) throws IOException {
        var localDate = objectMapper.readValue("\"2019-02-01\"", LocalDate.class);
        Assertions.assertThat(localDate).isEqualTo(LocalDate.of(2019, 2, 1));
    }
}
