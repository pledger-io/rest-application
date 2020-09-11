package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;

public class ScheduledTransactionResponse {

    private final ScheduledTransaction wrapped;

    public ScheduledTransactionResponse(final ScheduledTransaction wrapped) {
        this.wrapped = wrapped;
    }

    public long getId() {
        return wrapped.getId();
    }

    public String getName() {
        return wrapped.getName();
    }

    public String getDescription() {
        return wrapped.getDescription();
    }

    public DateRangeResponse getRange() {
        return new DateRangeResponse(DateRange.of(wrapped.getStart(), wrapped.getEnd()));
    }

    public ScheduleResponse getSchedule() {
        return new ScheduleResponse(wrapped.getSchedule());
    }

    private AccountResponse getSource() {
        return new AccountResponse(wrapped.getSource());
    }

    private AccountResponse getDestination() {
        return new AccountResponse(wrapped.getDestination());
    }

}
