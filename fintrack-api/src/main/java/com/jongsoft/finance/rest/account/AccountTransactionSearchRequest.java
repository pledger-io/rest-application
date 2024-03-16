package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Serdeable
class AccountTransactionSearchRequest {

    @Serdeable
    public record Range(LocalDate start, LocalDate end) {
    }

    private String text;

    @Min(0)
    private int page;

    @NotNull
    private Range dateRange;

    public AccountTransactionSearchRequest(String text, int page, Range dateRange) {
        this.text = text;
        this.page = page;
        this.dateRange = dateRange;
    }

    public String getText() {
        return text;
    }

    public Range getDateRange() {
        return dateRange;
    }

    public int getPage() {
        return Math.max(0, page - 1);
    }

}
