package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.date.DateRangeOld;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Generated;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Setter
@Introspected
class AccountTransactionSearchRequest {

    private String text;

    @Min(0)
    private int page;

    @NotNull
    private DateRangeOld dateRange;

    @Generated
    public AccountTransactionSearchRequest() {
    }

    @Builder
    private AccountTransactionSearchRequest(String text, @Min(0) int page, @NotNull DateRangeOld dateRange) {
        this.text = text;
        this.page = page;
        this.dateRange = dateRange;
    }

    public String getText() {
        return text;
    }

    public DateRangeOld getDateRange() {
        return dateRange;
    }

    public int getPage() {
        return Math.max(0, page - 1);
    }
}
