package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.providers.TransactionProvider;
import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDate;

@Serdeable.Serializable
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
