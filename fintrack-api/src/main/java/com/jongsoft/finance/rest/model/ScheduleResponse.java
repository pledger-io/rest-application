package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.schedule.Schedule;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class ScheduleResponse {

    private final Schedule wrapped;

    public ScheduleResponse(final Schedule wrapped) {
        this.wrapped = wrapped;
    }

    public Periodicity getPeriodicity() {
        return wrapped.periodicity();
    }

    public int getInterval() {
        return wrapped.interval();
    }
}
