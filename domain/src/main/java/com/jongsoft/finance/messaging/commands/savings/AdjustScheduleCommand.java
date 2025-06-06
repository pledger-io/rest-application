package com.jongsoft.finance.messaging.commands.savings;

import com.jongsoft.finance.messaging.commands.schedule.ScheduleCommand;
import com.jongsoft.finance.schedule.Schedulable;
import java.util.Map;

/** Change the schedule attached to a saving goal. */
public record AdjustScheduleCommand(long id, Schedulable schedulable) implements ScheduleCommand {

  @Override
  public String processDefinition() {
    return "ScheduledSavingGoal";
  }

  @Override
  public Map<String, Object> variables() {
    return Map.of("id", id);
  }

  public static void scheduleAdjusted(long id, Schedulable schedulable) {
    new AdjustScheduleCommand(id, schedulable).publish();
  }
}
