package com.jongsoft.finance.rest.transaction;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Serdeable
class TransactionSearchRequest {

    @Serdeable
    public record DateRange(
            @Schema(description = "Any matching transaction must be after this date")
            LocalDate start,
            @Schema(description = "Any matching transaction must be before this date")
            LocalDate end) {
    }

    @Serdeable
    public record EntityRef(
            @Schema(description = "The identifier of the relationship")
            long id) {
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
