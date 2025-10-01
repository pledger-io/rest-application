package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.schedule.Periodicity;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Serdeable
record CreateScheduleRequest(
        @NotNull @Schema(description = "The schedule to create transactions on.")
                ScheduleValueJson schedule,
        @NotNull @Schema(description = "The account to charge for every scheduled transaction.")
                EntityRef source,
        @Schema(
                        description = "The amount to charge for every scheduled transaction.",
                        example = "100.00")
                double amount) {

    @Serdeable
    public record ScheduleValueJson(
            @NotNull @Schema(description = "The periodicity of the schedule.", example = "MONTHS")
                    Periodicity periodicity,
            @Min(1)
                    @Schema(
                            description = "The interval a transaction should be created on.",
                            example = "2")
                    int interval) {}

    @Serdeable.Deserializable
    record EntityRef(
            @NotNull @Schema(description = "The id of the account.", example = "1") Long id,
            String name) {}

    public ScheduleValue getSchedule() {
        return new ScheduleValue(schedule.periodicity, schedule.interval);
    }
}
