package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.core.value.Schedulable;

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

    public static void scheduleCreated(
            long id, Schedulable schedulable, LocalDate start, LocalDate end) {
        new LimitScheduleCommand(id, schedulable, start, end).publish();
    }
}
