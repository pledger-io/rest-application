package com.jongsoft.finance.rest.model;

import com.jongsoft.lang.time.Range;
import io.micronaut.core.annotation.Introspected;

import java.time.LocalDate;

@Introspected
public class DateRangeResponse {

    private final Range<LocalDate> wrapped;

    public DateRangeResponse(final Range<LocalDate> wrapped) {
        this.wrapped = wrapped;
    }

    public LocalDate getStart() {
        return wrapped.from();
    }

    public LocalDate getEnd() {
        return wrapped.until();
    }
}
