package com.jongsoft.finance.rest.model;

import com.jongsoft.lang.time.Range;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Serdeable.Serializable
public class DateRangeResponse {

    private final Range<LocalDate> wrapped;

    public DateRangeResponse(final Range<LocalDate> wrapped) {
        this.wrapped = wrapped;
    }

    @Schema(description = "The start of the date range", example = "2020-01-01", required = true)
    public LocalDate getStart() {
        return wrapped.from();
    }

    @Schema(description = "The end of the date range", example = "2020-01-31", required = true)
    public LocalDate getEnd() {
        return wrapped.until();
    }
}
