package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Serdeable.Deserializable
public class ScheduledTransactionCreateRequest {

  @Serdeable.Deserializable
  public record EntityRef(@NotNull Long id, String name) {}

  @Serdeable.Deserializable
  public record ScheduleValue(@NotNull Periodicity periodicity, @Min(1) int interval) {}

  @NotBlank private final String name;

  private final double amount;

  @NotNull private final EntityRef source;

  @NotNull private final EntityRef destination;

  @NotNull private final ScheduleValue schedule;

  public ScheduledTransactionCreateRequest(
      String name, double amount, EntityRef source, EntityRef destination, ScheduleValue schedule) {
    this.name = name;
    this.amount = amount;
    this.source = source;
    this.destination = destination;
    this.schedule = schedule;
  }

  public com.jongsoft.finance.domain.transaction.ScheduleValue getSchedule() {
    return new com.jongsoft.finance.domain.transaction.ScheduleValue(
        schedule.periodicity, schedule.interval);
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
