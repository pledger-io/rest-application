package com.jongsoft.finance.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jongsoft.lang.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CollectionSerializerTest {

    @Test
    void serialize() throws IOException {
        var serializer = new CollectionSerializer();
        var generator = Mockito.mock(JsonGenerator.class);

        serializer.serialize(Collections.List("test", "two"), generator, Mockito.mock(SerializerProvider.class));

        Mockito.verify(generator).writeStartArray();
        Mockito.verify(generator).writeObject("test");
        Mockito.verify(generator).writeObject("two");
        Mockito.verify(generator).writeEndArray();
    }

}