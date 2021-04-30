package com.jongsoft.finance.messaging.commands.schedule;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.schedule.Schedule;

public record CreateScheduleCommand(String name, Schedule schedule, Account from, Account destination, double amount)
        implements ApplicationEvent {

}
