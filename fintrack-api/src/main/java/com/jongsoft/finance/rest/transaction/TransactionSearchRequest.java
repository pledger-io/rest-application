package com.jongsoft.finance.rest.transaction;

import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Builder
@Introspected
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchRequest {

    @Introspected
    public static class DateRange {
        @Schema(description = "Any matching transaction must be after this date")
        private LocalDate start;
        @Schema(description = "Any matching transaction must be before this date")
        private LocalDate end;

        public DateRange() {
        }

        public DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }

        public LocalDate getEnd() {
            return end;
        }

        public LocalDate getStart() {
            return start;
        }

    }

    @Data
    @Introspected
    public static class EntityRef {
        @Schema(description = "The identifier of the relationship")
        private long id;
    }

    @Schema(description = "The partial description the transaction should match", example = "saving tra")
    private String description;
    @Schema(description = "The partial name of one of the accounts involved in the transaction")
    private String account;
    @Schema(description = "The currency the transaction must have")
    private String currency;
    @Schema(description = "Only include transactions considered as expense from one own accounts")
    private boolean onlyExpense;
    @Schema(description = "Only include transactions considered as income from one own accounts")
    private boolean onlyIncome;

    @Schema(description = "The category that the transaction must have")
    private EntityRef category;
    @Schema(description = "The budget expense that the transaction must have")
    private EntityRef budget;

    @Min(0)
    @Schema(description = "Set the page number in the resulting pages")
    private int page;

    @Schema(description = "Only include transactions between one own accounts")
    private boolean transfers;

    @NotNull
    @Schema(description = "The range wherein the transaction date must be")
    private DateRange dateRange;

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

    public DateRange getDateRange() {
        return dateRange;
    }

    public int getPage() {
        return Math.max(0, page - 1);
    }
}
