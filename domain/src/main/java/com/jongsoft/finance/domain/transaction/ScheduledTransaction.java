package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.messaging.commands.schedule.CreateScheduleCommand;
import com.jongsoft.finance.messaging.commands.schedule.DescribeScheduleCommand;
import com.jongsoft.finance.messaging.commands.schedule.LimitScheduleCommand;
import com.jongsoft.finance.messaging.commands.schedule.RescheduleCommand;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.schedule.Schedulable;
import com.jongsoft.finance.schedule.Schedule;
import com.jongsoft.lang.Control;

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

    private Contract contract;

    private Schedule schedule;
    private LocalDate start;
    private LocalDate end;

    public ScheduledTransaction(
            String name, Schedule schedule, Account source, Account destination, double amount) {
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.amount = amount;
        this.schedule = schedule;

        CreateScheduleCommand.scheduleCreated(name, schedule, source, destination, amount);
    }

    @BusinessMethod
    public void limit(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw StatusException.badRequest(
                    "Start of scheduled transaction cannot be after end date.",
                    "validation.transaction.schedule.end.before.start");
        }

        var hasChanged = Control.Equal(this.start, start).append(this.end, end).isNotEqual();

        if (hasChanged) {
            this.start = start;
            this.end = end;
            LimitScheduleCommand.scheduleCreated(id, this, start, end);
        }
    }

    @BusinessMethod
    public void limitForContract() {
        if (contract == null) {
            throw StatusException.badRequest(
                    "Cannot limit based on a contract when no contract is set.");
        }

        var expectedEnd = contract.getEndDate().plusYears(20);
        if (end == null || !end.isEqual(expectedEnd)) {
            this.start = Control.Option(this.start).getOrSupply(contract::getStartDate);
            this.end = expectedEnd;
            LimitScheduleCommand.scheduleCreated(id, this, start, end);
        }
    }

    @BusinessMethod
    public void terminate() {
        if (this.start == null) {
            this.start = LocalDate.now().minusDays(1);
        }

        this.end = LocalDate.now();
        LimitScheduleCommand.scheduleCreated(id, this, start, end);
    }

    @BusinessMethod
    public void adjustSchedule(Periodicity periodicity, int interval) {
        var hasChanged = this.schedule == null
                || Control.Equal(this.schedule.interval(), interval)
                        .append(this.schedule.periodicity(), periodicity)
                        .isNotEqual();

        if (hasChanged) {
            this.schedule = new ScheduleValue(periodicity, interval);
            RescheduleCommand.scheduleRescheduled(id, this, schedule);
        }
    }

    @BusinessMethod
    public void describe(String name, String description) {
        var hasChanged = Control.Equal(this.name, name)
                .append(this.description, description)
                .isNotEqual();

        if (hasChanged) {
            this.name = name;
            this.description = description;
            DescribeScheduleCommand.scheduleDescribed(id, name, description);
        }
    }
}
