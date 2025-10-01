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

/** Custom serializer for {@link Character} type. */
@Primary
@Singleton
public class CharSerializer implements Serde<Character> {
    @Override
    public @Nullable Character deserialize(
            @NonNull Decoder decoder,
            @NonNull DecoderContext context,
            @NonNull Argument<? super Character> type)
            throws IOException {
        return decoder.decodeString().charAt(0);
    }

    @Override
    public void serialize(
            @NonNull Encoder encoder,
            @NonNull EncoderContext context,
            @NonNull Argument<? extends Character> type,
            @NonNull Character value)
            throws IOException {
        encoder.encodeString(value.toString());
    }
}
