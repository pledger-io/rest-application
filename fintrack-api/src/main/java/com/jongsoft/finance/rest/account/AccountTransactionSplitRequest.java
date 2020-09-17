package com.jongsoft.finance.rest.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
class AccountTransactionSplitRequest {

    @NoArgsConstructor
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
