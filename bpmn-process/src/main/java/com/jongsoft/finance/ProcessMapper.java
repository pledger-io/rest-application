package com.jongsoft.finance;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jongsoft.finance.serialized.ImportConfigJson;
import com.jongsoft.lang.API;
import com.jongsoft.lang.control.Try;
import com.jongsoft.language.fasterxml.LanguageModule;

public class ProcessMapper extends ObjectMapper {

    public static final ProcessMapper INSTANCE = new ProcessMapper();

    public ProcessMapper() {
        initialize();
    }

    public static <T> String writeSafe(T entity) {
        return API.Try(() -> INSTANCE.writeValueAsString(entity))
                .recover(x -> null)
                .get();
    }

    public static <T> T readSafe(String json, Class<T> clazz) {
        return API.Try(() -> INSTANCE.readValue(json, clazz))
                .recover(x -> null)
                .get();
    }

    public static <T> Try<T> read(String json, Class<T> clazz) {
        return API.Try(() -> INSTANCE.readValue(json, clazz));
    }

    private void initialize() {
        registerModule(new JavaTimeModule());
        registerModule(new Jdk8Module());
        registerModule(new LanguageModule());

        final SimpleModule module = new SimpleModule();
        module.addSerializer(ImportConfigJson.MappingRole.class, new JsonSerializer<>() {
            @Override
            public void serialize(ImportConfigJson.MappingRole value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.getLabel());
            }
        });
        module.addDeserializer(ImportConfigJson.MappingRole.class, new JsonDeserializer<>() {
            @Override
            public ImportConfigJson.MappingRole deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return ImportConfigJson.MappingRole.value(p.readValueAs(String.class));
            }
        });
        registerModule(module);
    }

}
