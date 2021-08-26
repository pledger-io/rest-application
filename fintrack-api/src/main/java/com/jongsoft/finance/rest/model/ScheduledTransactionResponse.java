package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.lang.Dates;
import io.micronaut.core.annotation.Introspected;

@Introspected
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
        return new DateRangeResponse(Dates.range(wrapped.getStart(), wrapped.getEnd()));
    }

    public ScheduleResponse getSchedule() {
        return new ScheduleResponse(wrapped.getSchedule());
    }

    public double getAmount() {
        return wrapped.getAmount();
    }

    public AccountResponse getSource() {
        return new AccountResponse(wrapped.getSource());
    }

    public AccountResponse getDestination() {
        return new AccountResponse(wrapped.getDestination());
    }

    public ContractResponse getContract() {
        if (wrapped.getContract() == null) {
            return null;
        }

        return new ContractResponse(wrapped.getContract());
    }

}
