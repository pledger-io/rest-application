package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import lombok.*;

import java.time.LocalDate;

@Builder
@Serdeable.Deserializable
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduledTransactionPatchRequest {

    @Serdeable.Deserializable
    public static class DateRange {
        private LocalDate start;
        private LocalDate end;

        public DateRange() {
        }

        public DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }

        public LocalDate getEnd() {
            return end;
        }

        public LocalDate getStart() {
            return start;
        }

    }

    @Data
    @Serdeable.Deserializable
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
