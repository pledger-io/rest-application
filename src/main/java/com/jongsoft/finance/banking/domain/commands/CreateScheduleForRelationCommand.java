package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.core.value.Schedule;

public record CreateScheduleForRelationCommand(
        String name, Schedule schedule, long contract, long companyId, double amount)
        implements ApplicationEvent {

    /**
     * Creates and publishes a new schedule for a contract using the provided information.
     *
     * @param name The name of the schedule.
     * @param schedule The schedule to be created.
     * @param contract The contract for which the schedule is created.
     * @param companyId The account to use as the source for the schedule.
     * @param amount The amount to use for the schedule.
     */
    public static void scheduleCreated(
            String name, Schedule schedule, long contract, long companyId, double amount) {
        new CreateScheduleForRelationCommand(name, schedule, contract, companyId, amount).publish();
    }
}
