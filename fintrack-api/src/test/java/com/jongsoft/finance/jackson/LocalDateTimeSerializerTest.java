package com.jongsoft.finance.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateTimeSerializerTest {

    @Test
    void serialize() throws IOException {
        var serializer = new LocalDateTimeSerializer();
        var generator = Mockito.mock(JsonGenerator.class);

        serializer.serialize(LocalDateTime.of(2019, 2, 1, 12, 12), generator, Mockito.mock(SerializerProvider.class));

        Mockito.verify(generator).writeString("2019-02-01T12:12:00");
    }

}