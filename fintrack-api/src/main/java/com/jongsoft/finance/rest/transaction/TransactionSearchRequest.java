package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.date.DateRangeOld;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Builder
@Introspected
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchRequest {

    @Data
    public static class EntityRef {
        private long id;
    }

    private String description;
    private String account;
    private String currency;
    private boolean onlyExpense;
    private boolean onlyIncome;

    private EntityRef category;
    private EntityRef budget;

    @Min(0)
    private int page;

    private boolean transfers;

    @NotNull
    private DateRangeOld dateRange;

    public String getDescription() {
        return description;
    }

    public String getAccount() {
        return account;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isOnlyExpense() {
        return onlyExpense;
    }

    public boolean isOnlyIncome() {
        return onlyIncome;
    }

    public EntityRef getCategory() {
        return category;
    }

    public EntityRef getBudget() {
        return budget;
    }

    public boolean isTransfers() {
        return transfers;
    }

    public DateRangeOld getDateRange() {
        return dateRange;
    }

    public int getPage() {
        return Math.max(0, page - 1);
    }
}
