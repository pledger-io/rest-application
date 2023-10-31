package com.jongsoft.finance.serialized.serde;

import com.jongsoft.finance.serialized.ImportConfigJson;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serde;
import jakarta.inject.Singleton;

import java.io.IOException;

@Singleton
public class MappingRoleSerde implements Serde<ImportConfigJson.MappingRole> {
    @Override
    public @Nullable ImportConfigJson.MappingRole deserialize(
            @NonNull Decoder decoder,
            @NonNull DecoderContext context,
            @NonNull Argument<? super ImportConfigJson.MappingRole> type) throws IOException {
        return ImportConfigJson.MappingRole.value(decoder.decodeString());
    }

    @Override
    public void serialize(
            @NonNull Encoder encoder,
            @NonNull EncoderContext context,
            @NonNull Argument<? extends ImportConfigJson.MappingRole> type, ImportConfigJson.@NonNull MappingRole value) throws IOException {
        encoder.encodeString(value.getLabel());
    }
}
