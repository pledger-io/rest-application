package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.core.date.DateRangeOld;

import java.time.LocalDate;

public class DateRangeResponse {

    private final DateRangeOld wrapped;

    public DateRangeResponse(final DateRangeOld wrapped) {
        this.wrapped = wrapped;
    }

    public LocalDate getStart() {
        return wrapped.getStart();
    }

    public LocalDate getEnd() {
        return wrapped.getEnd();
    }
}
