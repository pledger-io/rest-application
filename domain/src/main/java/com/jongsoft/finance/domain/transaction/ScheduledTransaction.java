package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.events.ScheduledTransactionCreatedEvent;
import com.jongsoft.finance.domain.transaction.events.ScheduledTransactionDescribeEvent;
import com.jongsoft.finance.domain.transaction.events.ScheduledTransactionLimitEvent;
import com.jongsoft.finance.domain.transaction.events.ScheduledTransactionRescheduleEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.schedule.Schedulable;
import com.jongsoft.finance.schedule.Schedule;
import com.jongsoft.lang.API;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduledTransaction implements AggregateBase, Schedulable {

    private Long id;

    private String name;
    private String description;
    private double amount;

    private Account source;
    private Account destination;

    private Schedule schedule;
    private LocalDate start;
    private LocalDate end;

    public ScheduledTransaction(String name, Schedule schedule, Account source, Account destination, double amount) {
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.amount = amount;
        this.schedule = schedule;

        EventBus.getBus().send(new ScheduledTransactionCreatedEvent(this, name, schedule,
                source, destination, amount));
    }

    @BusinessMethod
    public void limit(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start of scheduled transaction cannot be after end date.");
        }

        var hasChanged = API.Equal(this.start, start)
                .append(this.end, end)
                .isNotEqual();

        if (hasChanged) {
            this.start = start;
            this.end = end;
            EventBus.getBus().send(new ScheduledTransactionLimitEvent(this, id, start, end));
        }
    }

    @BusinessMethod
    public void adjustSchedule(Periodicity periodicity, int interval) {
        var hasChanged = this.schedule == null ||
                API.Equal(this.schedule.interval(), interval)
                .append(this.schedule.periodicity(), periodicity)
                .isNotEqual();

        if (hasChanged) {
            this.schedule = new ScheduleValue(periodicity, interval);
            EventBus.getBus().send(new ScheduledTransactionRescheduleEvent(this, id, schedule));
        }
    }

    @BusinessMethod
    public void describe(String name, String description) {
        var hasChanged = API.Equal(this.name, name)
                .append(this.description, description)
                .isNotEqual();

        if (hasChanged) {
            this.name = name;
            this.description = description;
            EventBus.getBus().send(new ScheduledTransactionDescribeEvent(this, id, description, name));
        }
    }
}
