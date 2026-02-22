package com.jongsoft.finance.banking.domain.model;

import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.core.value.Schedule;

public record ScheduleValue(Periodicity periodicity, int interval) implements Schedule {

    public static ScheduleValue of(Periodicity periodicity, int interval) {
        return new ScheduleValue(periodicity, interval);
    }
}
