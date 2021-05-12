package com.jongsoft.finance.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jongsoft.lang.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateSerializerTest {

    @Test
    void serialize() throws IOException {
        var serializer = new LocalDateSerializer();
        var generator = Mockito.mock(JsonGenerator.class);

        serializer.serialize(LocalDate.of(2019, 2, 1), generator, Mockito.mock(SerializerProvider.class));

        Mockito.verify(generator).writeString("2019-02-01");
    }

}