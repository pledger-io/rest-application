package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.providers.TransactionProvider;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Serdeable.Serializable
class DailyResponse {

    private final TransactionProvider.DailySummary wrapped;

    DailyResponse(TransactionProvider.DailySummary wrapped) {
        this.wrapped = wrapped;
    }

    @Schema(description = "The date of the summary.", implementation = String.class, format = "yyyy-mm-dd")
    public LocalDate getDate() {
        return wrapped.day();
    }

    @Schema(description = "The amount of money for the given date.")
    public double getAmount() {
        return wrapped.summary();
    }
}
