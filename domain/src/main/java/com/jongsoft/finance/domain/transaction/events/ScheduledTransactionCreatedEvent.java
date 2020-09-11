package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.schedule.Schedule;

import lombok.Getter;

@Getter
public class ScheduledTransactionCreatedEvent implements ApplicationEvent {

    private final String name;
    private final Schedule schedule;
    private final Account from;
    private final Account destination;
    private final double amount;

    public ScheduledTransactionCreatedEvent(Object source, String name, Schedule schedule,
                                            Account sourceAccount, Account destination, double amount) {
        this.name = name;
        this.schedule = schedule;
        this.from = sourceAccount;
        this.destination = destination;
        this.amount = amount;
    }

}
