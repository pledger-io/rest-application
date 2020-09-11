package com.jongsoft.finance.rest.model;

import java.time.LocalDate;

import com.jongsoft.finance.core.date.DateRange;

public class DateRangeResponse {

    private final DateRange wrapped;

    public DateRangeResponse(final DateRange wrapped) {
        this.wrapped = wrapped;
    }

    public LocalDate getStart() {
        return wrapped.getStart();
    }

    public LocalDate getEnd() {
        return wrapped.getEnd();
    }
}
