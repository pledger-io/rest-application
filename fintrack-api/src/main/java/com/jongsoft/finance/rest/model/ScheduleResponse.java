package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.schedule.Schedule;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;

@Introspected
public class ScheduleResponse {

    private final Schedule wrapped;

    public ScheduleResponse(final Schedule wrapped) {
        this.wrapped = wrapped;
    }

    @Schema(description = "The type of the interval", required = true, example = "MONTHS")
    public Periodicity getPeriodicity() {
        return wrapped.periodicity();
    }

    @Schema(description = "The actual interval", required = true, example = "3")
    public int getInterval() {
        return wrapped.interval();
    }
}
