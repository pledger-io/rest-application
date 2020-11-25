package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.core.date.DateRangeOld;
import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import javax.validation.Valid;

@Builder
@Introspected
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduledTransactionPatchRequest {

    @Data
    static class ScheduleValue {
        private Periodicity periodicity;
        private int interval;
    }

    @Valid
    private DateRangeOld range;

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

    public DateRangeOld getRange() {
        return range;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
