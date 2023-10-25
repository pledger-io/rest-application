package com.jongsoft.finance.jackson;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serde;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.time.LocalDate;

@Primary
@Singleton
public class LocalDateSerializer implements Serde<LocalDate> {

    @Override
    public @Nullable LocalDate deserialize(
            Decoder decoder,
            DecoderContext context,
            Argument<? super LocalDate> type) throws IOException {
        return LocalDate.parse(decoder.decodeString());
    }

    @Override
    public void serialize(
            Encoder encoder,
            EncoderContext context,
            Argument<? extends LocalDate> type,
            LocalDate value) throws IOException {
        encoder.encodeString(value.toString());
    }
}
