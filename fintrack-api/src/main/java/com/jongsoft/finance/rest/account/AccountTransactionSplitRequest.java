package com.jongsoft.finance.rest.account;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

class AccountTransactionSplitRequest {

    public static final class SplitRecord {
        private String description;
        private double amount;

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
