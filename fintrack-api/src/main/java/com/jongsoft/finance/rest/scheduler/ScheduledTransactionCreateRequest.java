package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Builder
@Serdeable.Deserializable
public class ScheduledTransactionCreateRequest {

    @Builder
    @Serdeable.Deserializable
    static class EntityRef {
        @NotNull
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @Serdeable.Deserializable
    record ScheduleValue(Periodicity periodicity, int interval) {}

    @NotBlank
    private String name;

    private double amount;

    @NotNull
    private EntityRef source;

    @NotNull
    private EntityRef destination;

    @NotNull
    private ScheduleValue schedule;

    public com.jongsoft.finance.domain.transaction.ScheduleValue getSchedule() {
        return new com.jongsoft.finance.domain.transaction.ScheduleValue(schedule.periodicity, schedule.interval);
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public EntityRef getSource() {
        return source;
    }

    public EntityRef getDestination() {
        return destination;
    }
}
