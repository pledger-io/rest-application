package com.jongsoft.finance.domain.account;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.account.events.*;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.schedule.Schedule;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.function.Consumer;

@Getter
@Builder
@Aggregate
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account implements AggregateBase, Serializable {

    private Long id;
    private UserAccount user;

    private String name;
    private String description;
    private String currency;

    private String iban;
    private String bic;
    private String number;
    private String type;

    private String imageFileToken;

    private double balance;
    private LocalDate lastTransaction;
    private LocalDate firstTransaction;

    private double interest;
    private Periodicity interestPeriodicity;

    private boolean remove;

    @BusinessMethod
    public Account(UserAccount user, String name, String currency, String type) {
        this.user = user;
        this.name = name;
        this.currency = currency;
        this.type = type;

        EventBus.getBus().send(
                new AccountCreatedEvent(
                        user,
                        user,
                        name,
                        currency,
                        type));
    }

    @BusinessMethod
    public void rename(String name, String description, String currency, String type) {
        var noChanges = Control.Equal(this.name, name)
                .append(this.description, description)
                .append(this.currency, currency)
                .append(this.type, type)
                .isEqual();

        if (!noChanges) {
            this.name = name;
            this.description = description;
            this.currency = currency;
            this.type = type;

            EventBus.getBus().send(
                    new AccountRenamedEvent(
                            this,
                            user,
                            id,
                            type,
                            name,
                            description,
                            currency));
        }
    }

    @BusinessMethod
    public void registerIcon(String fileCode) {
        EventBus.getBus().send(
                new AccountIconAttachedEvent(
                        id,
                        fileCode,
                        this.imageFileToken));

        this.imageFileToken = fileCode;
    }

    @BusinessMethod
    public void interest(double interest, Periodicity periodicity) {
        if (interest < -2 || interest > 2) {
            throw new IllegalArgumentException("Highly improbable interest of more than 200%.");
        }

        var changes = Control.Equal(this.interest, interest)
                .append(this.interestPeriodicity, periodicity)
                .isNotEqual();

        if (changes) {
            this.interest = interest;
            this.interestPeriodicity = periodicity;

            EventBus.getBus().send(
                    new AccountInterestEvent(
                            this,
                            id,
                            interest,
                            interestPeriodicity));
        }
    }

    @BusinessMethod
    public void registerSynonym(String synonym) {
        EventBus.getBus().send(
                new AccountSynonymEvent(
                        this,
                        user,
                        synonym,
                        id));
    }

    /**
     * Modify the banking detail information, being account number, IBAN and BIC.
     *
     * @param iban      the new IBAN
     * @param bic       the new BIC
     * @param number    the account number
     */
    @BusinessMethod
    public void changeAccount(String iban, String bic, String number) {
        var noChanges = Control.Equal(this.iban, iban)
                .append(this.bic, bic)
                .append(this.number, number)
                .isEqual();

        if (!noChanges) {
            this.iban = iban;
            this.bic = bic;
            this.number = number;
            EventBus.getBus().send(
                    new AccountChangedEvent(
                            this,
                            user,
                            id,
                            iban,
                            bic,
                            number));
        }
    }

    /**
     * Close this account, making it archived and no longer accessible.
     */
    @BusinessMethod
    public void terminate() {
        remove = true;
        EventBus.getBus().send(new AccountTerminatedEvent(this, this));
    }

    @BusinessMethod
    public Transaction createTransaction(
            Account to,
            double amount,
            Transaction.Type transactionType,
            Consumer<Transaction.TransactionBuilder> applier) {
        Account destination;
        Account source;
        switch (transactionType) {
            case DEBIT -> {
                source = to;
                destination = this;
            }
            case CREDIT, TRANSFER -> {
                source = this;
                destination = to;
            }
            default -> throw new IllegalArgumentException();
        }

        var builder = new Transaction(
                user,
                source,
                destination,
                amount)
                .toBuilder();
        applier.accept(builder);
        return builder.build();
    }

    /**
     * Create a new scheduled transaction with the current account being the source account.
     *
     * @param name the name of the schedule
     * @param schedule the schedule to adhere to
     * @param destination the destination account
     * @param amount the amount involved in the transactions
     * @return the newly created scheduled transaction (not yet persisted)
     */
    @BusinessMethod
    public ScheduledTransaction createSchedule(String name, Schedule schedule, Account destination, double amount) {
        return new ScheduledTransaction(
                name,
                schedule,
                this,
                destination,
                amount);
    }

    /**
     * Create a new contract for this account.
     *
     * @param name  the name of the contract
     * @param start the start date of the contract
     * @param end   the end date of the contract
     * @return  the newly created contract
     */
    @BusinessMethod
    public Contract createContract(String name, String description, LocalDate start, LocalDate end) {
        return new Contract(
                this,
                name,
                description,
                start,
                end);
    }

    /**
     * A managed account is one that is owned by the user and transactions are linked to it.
     *
     * @return true if managed
     */
    public boolean isManaged() {
        return !Collections.List(SystemAccountTypes.values())
                .map(SystemAccountTypes::label)
                .contains(type.toLowerCase());
    }

    @Override
    public String toString() {
        return getName();
    }

}
