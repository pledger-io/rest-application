package com.jongsoft.finance.jackson;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serde;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Primary
@Singleton
public class LocalDateTimeSerializer implements Serde<LocalDateTime> {

    @Override
    public @Nullable LocalDateTime deserialize(
            @NonNull Decoder decoder,
            @NonNull DecoderContext context,
            @NonNull Argument<? super LocalDateTime> type) throws IOException {
        return LocalDateTime.parse(decoder.decodeString());
    }

    @Override
    public void serialize(
            @NonNull Encoder encoder,
            @NonNull EncoderContext context,
            @NonNull Argument<? extends LocalDateTime> type,
            @NonNull LocalDateTime value) throws IOException {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
