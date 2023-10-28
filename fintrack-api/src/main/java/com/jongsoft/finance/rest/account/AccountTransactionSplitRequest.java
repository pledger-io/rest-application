package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
@Serdeable.Deserializable
class AccountTransactionSplitRequest {

    @Serdeable.Deserializable
    public record SplitRecord(
            String description,
            double amount)
    {}

    @NotNull
    @Size(min = 2)
    private List<SplitRecord> splits;

    public List<SplitRecord> getSplits() {
        return splits;
    }
}
