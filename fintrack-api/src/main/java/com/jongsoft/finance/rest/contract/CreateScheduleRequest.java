package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Serdeable
class CreateScheduleRequest {

    @Serdeable
    public record ScheduleValueJson(
            @NotNull
            @Schema(description = "The periodicity of the schedule.", example = "MONTHS")
            Periodicity periodicity,
            @Min(1)
            @Schema(description = "The interval a transaction should be created on.", example = "2")
            int interval) {
    }

    @Serdeable.Deserializable
    record EntityRef(@NotNull
                     @Schema(description = "The id of the account.", example = "1")
                     Long id,
                     String name) {
    }

    @NotNull
    @Schema(description = "The schedule to create transactions on.")
    private ScheduleValueJson schedule;

    @NotNull
    @Schema(description = "The account to charge for every scheduled transaction.")
    private EntityRef source;

    @Schema(description = "The amount to charge for every scheduled transaction.", example = "100.00")
    private double amount;

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
