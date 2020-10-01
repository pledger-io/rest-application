package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Introspected
@NoArgsConstructor
public class ScheduledTransactionCreateRequest {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Data
    static class ScheduleValue {
        private Periodicity periodicity;
        private int interval;
    }

    @NotBlank
    private String name;

    private double amount;

    @NotNull
    private EntityRef source;

    @NotNull
    private EntityRef destination;

    @NotNull
    private ScheduleValue schedule;

    @Builder
    private ScheduledTransactionCreateRequest(@NotBlank final String name, final double amount, @NotNull final EntityRef source,
            @NotNull final EntityRef destination, @NotNull final ScheduleValue schedule) {
        this.name = name;
        this.amount = amount;
        this.source = source;
        this.destination = destination;
        this.schedule = schedule;
    }

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
