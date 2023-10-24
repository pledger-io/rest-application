package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Generated;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Serdeable.Deserializable
class AccountTransactionSearchRequest {

    @Setter
    @Serdeable.Deserializable
    public static class Range {
        private LocalDate start;
        private LocalDate end;

        public LocalDate getStart() {
            return start;
        }

        public LocalDate getEnd() {
            return end;
        }
    }

    private String text;

    @Min(0)
    private int page;

    @NotNull
    private Range dateRange;

    @Generated
    public AccountTransactionSearchRequest() {
    }

    @Builder
    private AccountTransactionSearchRequest(String text, @Min(0) int page, @NotNull Range dateRange) {
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
