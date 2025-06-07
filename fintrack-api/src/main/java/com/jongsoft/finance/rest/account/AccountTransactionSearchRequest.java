package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Serdeable
record AccountTransactionSearchRequest(String text, @Min(0) int page, @NotNull Range dateRange) {

  @Serdeable
  public record Range(LocalDate start, LocalDate end) {}

  public int getPage() {
    return Math.max(0, page - 1);
  }
}
