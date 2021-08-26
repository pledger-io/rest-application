package com.jongsoft.finance.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jongsoft.lang.collection.Collection;
import jakarta.inject.Singleton;

import java.io.IOException;

@Singleton
public class CollectionSerializer extends JsonSerializer<Collection<?>> {

    @Override
    public void serialize(Collection<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();

        for (Object entity : value) {
            gen.writeObject(entity);
        }

        gen.writeEndArray();
    }

}
