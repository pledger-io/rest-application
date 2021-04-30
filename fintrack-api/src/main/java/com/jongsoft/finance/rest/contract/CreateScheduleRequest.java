package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Introspected
class CreateScheduleRequest {

    @Data
    static class ScheduleValueJson {
        private Periodicity periodicity;
        private int interval;

        public ScheduleValueJson() {
        }

        public ScheduleValueJson(Periodicity periodicity, int interval) {
            this.periodicity = periodicity;
            this.interval = interval;
        }
    }

    @Data
    static class EntityRef {
        @NotNull
        private Long id;
        private String name;

        public EntityRef() {
        }

        public EntityRef(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @NotNull
    private ScheduleValueJson schedule;

    @NotNull
    private EntityRef source;

    private double amount;

    public CreateScheduleRequest() {
    }

    public CreateScheduleRequest(ScheduleValueJson schedule, EntityRef source, double amount) {
        this.schedule = schedule;
        this.source = source;
        this.amount = amount;
    }

    public ScheduleValue getSchedule() {
        return new ScheduleValue(
                schedule.periodicity,
                schedule.interval);
    }

    public EntityRef getSource() {
        return source;
    }

    public double getAmount() {
        return amount;
    }
}
