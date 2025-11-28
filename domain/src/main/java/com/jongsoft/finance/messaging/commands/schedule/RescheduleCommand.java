package com.jongsoft.finance.messaging.commands.schedule;

import com.jongsoft.finance.schedule.Schedulable;
import com.jongsoft.finance.schedule.Schedule;

import java.util.Map;

public record RescheduleCommand(long id, Schedulable schedulable, Schedule schedule)
        implements ScheduleCommand {

    @Override
    public String processDefinition() {
        return "ScheduledTransaction";
    }

    @Override
    public Map<String, Object> variables() {
        return Map.of("id", id);
    }

    public static void scheduleRescheduled(long id, Schedulable schedulable, Schedule schedule) {
        new RescheduleCommand(id, schedulable, schedule).publish();
    }
}
