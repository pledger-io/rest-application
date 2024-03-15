package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Serdeable.Deserializable
record AccountTransactionSplitRequest (
        @NotNull
        @Size(min = 2)
        List<SplitRecord> splits) {

    @Serdeable.Deserializable
    public record SplitRecord(
            String description,
            double amount)
    {}

    public List<SplitRecord> getSplits() {
        return splits;
    }
}
