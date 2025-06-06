package com.jongsoft.finance.rest.transaction;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Serdeable
class TransactionSearchRequest {

  @Serdeable
  public record DateRange(
      @Schema(description = "Any matching transaction must be after this date") LocalDate start,
      @Schema(description = "Any matching transaction must be before this date") LocalDate end) {}

  @Serdeable
  public record EntityRef(@Schema(description = "The identifier of the relationship") long id) {}

  @Schema(
      description = "The partial description the transaction should match",
      example = "saving tra")
  private final String description;

  @Schema(description = "The partial name of one of the accounts involved in the transaction")
  private final String account;

  @Schema(description = "The currency the transaction must have")
  private final String currency;

  @Schema(description = "Only include transactions considered as expense from one own accounts")
  private final boolean onlyExpense;

  @Schema(description = "Only include transactions considered as income from one own accounts")
  private final boolean onlyIncome;

  @Schema(description = "The category that the transaction must have")
  private final EntityRef category;

  @Schema(description = "The budget expense that the transaction must have")
  private final EntityRef budget;

  @Min(0)
  @Schema(description = "Set the page number in the resulting pages")
  private final int page;

  @Schema(description = "Only include transactions between one own accounts")
  private final boolean transfers;

  @NotNull
  @Schema(description = "The range wherein the transaction date must be")
  private final DateRange dateRange;

  TransactionSearchRequest(
      String description,
      String account,
      String currency,
      boolean onlyExpense,
      boolean onlyIncome,
      EntityRef category,
      EntityRef budget,
      int page,
      boolean transfers,
      DateRange dateRange) {
    this.description = description;
    this.account = account;
    this.currency = currency;
    this.onlyExpense = onlyExpense;
    this.onlyIncome = onlyIncome;
    this.category = category;
    this.budget = budget;
    this.page = page;
    this.transfers = transfers;
    this.dateRange = dateRange;
  }

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
