package com.jongsoft.finance.importer.csv.serde;

import com.jongsoft.finance.importer.csv.ColumnRole;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serde;
import jakarta.inject.Singleton;

import java.io.IOException;

@Singleton
class ColumnRoleSerde implements Serde<ColumnRole> {
    @Override
    public ColumnRole deserialize(Decoder decoder, DecoderContext context, Argument<? super ColumnRole> type) throws IOException {
        return ColumnRole.value(decoder.decodeString());
    }

    @Override
    public void serialize(Encoder encoder, EncoderContext context, Argument<? extends ColumnRole> type, ColumnRole value) throws IOException {
        encoder.encodeString(value.getLabel());
    }
}
