package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.domain.transaction.TransactionProvider;
import io.micronaut.core.annotation.Introspected;

import java.time.LocalDate;

@Introspected
class DailyResponse {

    private final TransactionProvider.DailySummary wrapped;

    DailyResponse(TransactionProvider.DailySummary wrapped) {
        this.wrapped = wrapped;
    }

    public LocalDate getDate() {
        return wrapped.day();
    }

    public double getAmount() {
        return wrapped.summary();
    }
}
