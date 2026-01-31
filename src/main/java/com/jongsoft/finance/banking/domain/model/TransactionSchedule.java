package com.jongsoft.finance.banking.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.core.value.Schedulable;
import com.jongsoft.finance.core.value.Schedule;
import com.jongsoft.lang.Control;

import io.micronaut.core.annotation.Introspected;

import java.time.LocalDate;

@Introspected
public class TransactionSchedule implements Schedulable {

    private Long id;

    private String name;
    private String description;
    private double amount;

    private Account source;
    private Account destination;

    // private Contract contract;

    private Schedule schedule;
    private LocalDate start;
    private LocalDate end;
    private LocalDate lastRun;
    private LocalDate nextRun;
    private boolean deleted;

    private TransactionSchedule(
            String name, Schedule schedule, Account source, Account destination, double amount) {
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.amount = amount;
        this.schedule = schedule;

        CreateScheduleCommand.scheduleCreated(
                name, schedule, source.getId(), destination.getId(), amount);
    }

    TransactionSchedule(
            Long id,
            String name,
            String description,
            double amount,
            Account source,
            Account destination,
            Schedule schedule,
            LocalDate start,
            LocalDate end,
            LocalDate lastRun,
            LocalDate nextRun) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.source = source;
        this.destination = destination;
        this.schedule = schedule;
        this.start = start;
        this.end = end;
        this.lastRun = lastRun;
        this.nextRun = nextRun;
    }

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

    // TODO move logic to contract module
    //    public void limitForContract() {
    //        if (contract == null) {
    //            throw StatusException.badRequest(
    //                    "Cannot limit based on a contract when no contract is set.");
    //        }
    //
    //        var expectedEnd = contract.getEndDate().plusYears(20);
    //        if (end == null || !end.isEqual(expectedEnd)) {
    //            this.start = Control.Option(this.start).getOrSupply(contract::getStartDate);
    //            this.end = expectedEnd;
    //            LimitScheduleCommand.scheduleCreated(id, this, start, end);
    //        }
    //    }

    public void terminate() {
        if (this.start == null) {
            this.start = LocalDate.now().minusDays(1);
        }

        this.end = LocalDate.now();
        LimitScheduleCommand.scheduleCreated(id, this, start, end);
    }

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

    public void reschedule() {
        lastRun = LocalDate.now();
        nextRun = schedule.next(lastRun);
        TransactionScheduleRan.scheduleRan(id, lastRun, nextRun);
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public Account getSource() {
        return source;
    }

    public Account getDestination() {
        return destination;
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public LocalDate getStart() {
        return start;
    }

    @Override
    public LocalDate getEnd() {
        return end;
    }

    public boolean shouldCreateTransaction() {
        return nextRun == null || nextRun.isBefore(LocalDate.now());
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDate getLastRun() {
        return lastRun;
    }

    public LocalDate getNextRun() {
        return nextRun;
    }

    public static TransactionSchedule create(
            String name, Schedule schedule, Account source, Account destination, double amount) {
        return new TransactionSchedule(name, schedule, source, destination, amount);
    }
}
