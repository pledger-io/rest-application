package com.jongsoft.finance.messaging.commands.schedule;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.messaging.ApplicationEvent;
import com.jongsoft.finance.schedule.Schedule;

public record CreateScheduleForContractCommand(String name,
                                               Schedule schedule,
                                               Contract contract,
                                               Account source,
                                               double amount) implements ApplicationEvent {

    /**
     * Creates and publishes a new schedule for a contract using the provided information.
     *
     * @param name The name of the schedule.
     * @param schedule The schedule to be created.
     * @param contract The contract for which the schedule is created.
     * @param source The account to use as the source for the schedule.
     * @param amount The amount to use for the schedule.
     */
    public static void scheduleCreated(String name, Schedule schedule, Contract contract, Account source, double amount) {
        new CreateScheduleForContractCommand(name, schedule, contract, source, amount)
                .publish();
    }
}
