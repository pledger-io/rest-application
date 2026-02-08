package com.jongsoft.finance.contract.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.domain.commands.CreateScheduleForRelationCommand;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.TransactionSchedule;
import com.jongsoft.finance.contract.domain.commands.*;
import com.jongsoft.finance.core.value.Schedule;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;
import java.time.LocalDate;

@Introspected
public class Contract implements Serializable, Classifier {

    private Long id;
    private String name;
    private String description;
    private Account company;

    private LocalDate startDate;
    private LocalDate endDate;

    private String fileToken;
    private TransactionSchedule schedule;

    private boolean uploaded;
    private boolean notifyBeforeEnd;
    private boolean notificationSend;
    private boolean terminated;

    // Used by the Mapper strategy
    Contract(
            Long id,
            String name,
            String description,
            Account company,
            LocalDate startDate,
            LocalDate endDate,
            String fileToken,
            TransactionSchedule schedule,
            boolean uploaded,
            boolean notifyBeforeEnd,
            boolean terminated) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.company = company;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fileToken = fileToken;
        this.uploaded = uploaded;
        this.notifyBeforeEnd = notifyBeforeEnd;
        this.terminated = terminated;
        this.schedule = schedule;
    }

    private Contract(
            Account company, String name, String description, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start cannot be after end of contract.");
        }

        this.name = name;
        this.startDate = start;
        this.endDate = end;
        this.company = company;
        this.description = description;
        CreateContractCommand.contractCreated(company.getId(), name, description, start, end);
    }

    /**
     * Creates a schedule for this contract. For each period in the schedule, a transaction will be
     * created automatically.
     *
     * @param schedule The schedule to create.
     * @param source The account to use as source for the schedule.
     * @param amount The amount to use for the schedule.
     */
    public void createSchedule(Schedule schedule, Account source, double amount) {
        CreateScheduleForRelationCommand.scheduleCreated(
                name, schedule, id, source.getId(), amount);
    }

    public void change(String name, String description, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start cannot be after end of contract.");
        }

        this.name = name;
        this.startDate = start;
        this.endDate = end;
        this.description = description;

        ChangeContractCommand.contractChanged(id, name, description, start, end);
    }

    /**
     * Activates the warning before the ending for this contract. If the contract is not yet
     * persisted, or has already expired, an exception will be thrown.
     */
    public void warnBeforeExpires() {
        if (id == null) {
            throw new IllegalStateException(
                    "Cannot activate contract warning if contract is not yet persisted.");
        }

        if (endDate.isBefore(LocalDate.now())) {
            throw StatusException.badRequest(
                    "Cannot activate contract warning if contract has expired.",
                    "contract.warn.not.possible.expired");
        }

        if (!notifyBeforeEnd) {
            this.notifyBeforeEnd = true;
            WarnBeforeExpiryCommand.warnBeforeExpiry(id, endDate);
        }
    }

    public void notificationSend() {
        if (id == null) {
            throw new IllegalStateException(
                    "Cannot execute contract warning if contract is not yet persisted.");
        }

        if (!notifyBeforeEnd) {
            throw StatusException.internalError(
                    "Cannot send notification if warning is not active.");
        }

        if (!notificationSend) {
            this.notificationSend = true;
            ContractWarningSend.warningSent(id);
        }
    }

    public void registerUpload(String storageToken) {
        if (uploaded) {
            throw new IllegalStateException("Contract still contains upload.");
        }

        this.uploaded = true;
        AttachFileToContractCommand.attachFileToContract(id, storageToken);
    }

    public void terminate() {
        if (terminated) {
            throw StatusException.badRequest(
                    "Contract is already terminated.", "contract.already.terminated");
        }

        if (endDate.isAfter(LocalDate.now())) {
            throw StatusException.badRequest(
                    "Contract has not yet expired.", "contract.not.expired");
        }

        if (schedule != null) {
            schedule.terminate();
        }

        this.terminated = true;
        TerminateContractCommand.contractTerminated(id);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Account getCompany() {
        return company;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getFileToken() {
        return fileToken;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public boolean isNotifyBeforeEnd() {
        return notifyBeforeEnd;
    }

    public boolean isNotificationSend() {
        return notificationSend;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public TransactionSchedule getSchedule() {
        return schedule;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public static Contract create(
            Account company, String name, String description, LocalDate start, LocalDate end) {
        return new Contract(company, name, description, start, end);
    }
}
