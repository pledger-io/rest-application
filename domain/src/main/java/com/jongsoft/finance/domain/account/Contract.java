package com.jongsoft.finance.domain.account;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.contract.*;
import com.jongsoft.finance.messaging.commands.schedule.CreateScheduleForContractCommand;
import com.jongsoft.finance.schedule.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Builder
@Aggregate
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Contract implements AggregateBase, Serializable {

    private Long id;
    private String name;
    private String description;
    private Account company;

    private LocalDate startDate;
    private LocalDate endDate;

    private String fileToken;

    private boolean uploaded;
    private boolean notifyBeforeEnd;
    private boolean terminated;

    Contract(Account company, String name, String description, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start cannot be after end of contract.");
        }

        this.name = name;
        this.startDate = start;
        this.endDate = end;
        this.company = company;
        this.description = description;

        EventBus.getBus().send(new CreateContractCommand(company.getId(), name, description, start, end));
    }

    @BusinessMethod
    public void createSchedule(Schedule schedule, Account source, double amount) {
        EventBus.getBus().send(new CreateScheduleForContractCommand(
                this.name,
                schedule,
                this,
                source,
                amount));
    }

    @BusinessMethod
    public void change(String name, String description, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start cannot be after end of contract.");
        }

        this.name = name;
        this.startDate = start;
        this.endDate = end;
        this.description = description;

        EventBus.getBus().send(new ChangeContractCommand(id, name, description, start, end));
    }

    @BusinessMethod
    public void warnBeforeExpires() {
        if (id == null) {
            throw new IllegalStateException("Cannot activate contract warning if contract is not yet persisted.");
        }

        if (endDate.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot activate contract warning if contract has expired.");
        }

        if (!notifyBeforeEnd) {
            this.notifyBeforeEnd = true;
            EventBus.getBus().send(new WarnBeforeExpiryCommand(id, endDate));
        }
    }

    @BusinessMethod
    public void registerUpload(String storageToken) {
        if (uploaded) {
            throw new IllegalStateException("Contract still contains upload.");
        }

        this.uploaded = true;
        EventBus.getBus().send(new AttachFileToContractCommand(id, storageToken));
    }

    @BusinessMethod
    public void terminate() {
        if (terminated) {
            throw new IllegalStateException("Contract is already terminated.");
        }

        if (endDate.isAfter(LocalDate.now())) {
            throw new IllegalStateException("Contract has not yet expired.");
        }

        this.terminated = true;
        EventBus.getBus().send(new TerminateContract(id));
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
