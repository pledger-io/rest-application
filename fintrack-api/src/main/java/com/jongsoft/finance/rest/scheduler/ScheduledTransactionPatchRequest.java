package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;

import java.time.LocalDate;

@Serdeable.Deserializable
public class ScheduledTransactionPatchRequest {

    public ScheduledTransactionPatchRequest(DateRange range, ScheduleValue schedule, String name, String description) {
        this.range = range;
        this.schedule = schedule;
        this.name = name;
        this.description = description;
    }

    @Serdeable.Deserializable
    public record DateRange(LocalDate start, LocalDate end) {
    }

    @Serdeable.Deserializable
    public record ScheduleValue(Periodicity periodicity, int interval) {
    }

    @Valid
    private final DateRange range;

    @Valid
    private final ScheduleValue schedule;

    private final String name;
    private final String description;

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
