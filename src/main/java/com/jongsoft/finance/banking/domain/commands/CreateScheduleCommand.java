package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.core.value.Schedule;

public record CreateScheduleCommand(
        String name, Schedule schedule, long from, long destination, double amount)
        implements ApplicationEvent {

    /**
     * Publishes an event indicating that a schedule has been created with the specified details.
     *
     * @param name the name of the schedule
     * @param schedule the schedule object containing the periodicity and interval information
     * @param from the source account identifier
     * @param destination the destination account identifier
     * @param amount the monetary amount associated with the schedule
     */
    public static void scheduleCreated(
            String name, Schedule schedule, long from, long destination, double amount) {
        new CreateScheduleCommand(name, schedule, from, destination, amount).publish();
    }
}
