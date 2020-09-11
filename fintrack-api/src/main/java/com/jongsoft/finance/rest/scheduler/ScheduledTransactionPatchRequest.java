package com.jongsoft.finance.rest.scheduler;

import javax.validation.Valid;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.schedule.Periodicity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduledTransactionPatchRequest {

    @Data
    static class ScheduleValue {
        private Periodicity periodicity;
        private int interval;
    }

    @Valid
    private DateRange range;

    @Valid
    private ScheduleValue schedule;

    private String name;
    private String description;

    public com.jongsoft.finance.domain.transaction.ScheduleValue getSchedule() {
        if (schedule != null) {
            return new com.jongsoft.finance.domain.transaction.ScheduleValue(schedule.periodicity, schedule.interval);
        }

        return null;
    }

    public DateRange getRange() {
        return range;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
