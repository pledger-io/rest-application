package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.date.DateRange;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Setter
@NoArgsConstructor
class AccountTransactionSearchRequest {

    private String text;

    @Min(0)
    private int page;

    @NotNull
    private DateRange dateRange;

    @Builder
    private AccountTransactionSearchRequest(String text, @Min(0) int page, @NotNull DateRange dateRange) {
        this.text = text;
        this.page = page;
        this.dateRange = dateRange;
    }

    public String getText() {
        return text;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public int getPage() {
        return Math.max(0, page - 1);
    }
}
