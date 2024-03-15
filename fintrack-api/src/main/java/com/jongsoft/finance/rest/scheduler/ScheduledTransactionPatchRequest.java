package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;

import java.time.LocalDate;

@Serdeable.Deserializable
public class ScheduledTransactionPatchRequest {

    @Serdeable.Deserializable
    public record DateRange(LocalDate start, LocalDate end) {
    }

    @Serdeable.Deserializable
    public record ScheduleValue(Periodicity periodicity, int interval) {
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
