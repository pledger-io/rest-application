package com.jongsoft.finance.messaging.commands.schedule;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.messaging.ApplicationEvent;
import com.jongsoft.finance.schedule.Schedule;

public record CreateScheduleCommand(
        String name, Schedule schedule, Account from, Account destination, double amount)
        implements ApplicationEvent {

    public static void scheduleCreated(
            String name, Schedule schedule, Account from, Account destination, double amount) {
        new CreateScheduleCommand(name, schedule, from, destination, amount).publish();
    }
}
