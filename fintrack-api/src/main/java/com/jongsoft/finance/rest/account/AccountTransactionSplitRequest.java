package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Serdeable.Deserializable
@NoArgsConstructor
@AllArgsConstructor
class AccountTransactionSplitRequest {

    @NoArgsConstructor
    @Serdeable.Deserializable
    public static final class SplitRecord {
        private String description;
        private double amount;

        @Builder
        private SplitRecord(String description, double amount) {
            this.description = description;
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public double getAmount() {
            return amount;
        }
    }

    @NotNull
    @Size(min = 2)
    private List<SplitRecord> splits;

    public List<SplitRecord> getSplits() {
        return splits;
    }
}
