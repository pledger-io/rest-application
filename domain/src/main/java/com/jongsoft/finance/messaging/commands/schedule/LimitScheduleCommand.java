package com.jongsoft.finance.messaging.commands.schedule;

import com.jongsoft.finance.schedule.Schedulable;

import java.time.LocalDate;
import java.util.Map;

public record LimitScheduleCommand(long id, Schedulable schedulable, LocalDate start, LocalDate end)
        implements ScheduleCommand {

    @Override
    public String processDefinition() {
        return "ScheduledTransaction";
    }

    @Override
    public Map<String, Object> variables() {
        return Map.of("id", id);
    }
}
