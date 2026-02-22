package com.jongsoft.finance.banking.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.banking.types.SystemAccountTypes;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Set;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Introspected
public class Account implements Serializable, Classifier {

    private Long id;
    private UserIdentifier user;

    private String name;
    private String description;
    private String currency;

    private String iban;
    private String bic;
    private String number;
    private String type;

    private String imageFileToken;

    private LocalDate lastTransaction;
    private LocalDate firstTransaction;

    private double interest;
    private Periodicity interestPeriodicity;
    private Set<SavingGoal> savingGoals;

    private boolean remove;

    private Account(String name, String currency, String type) {
        this.name = name;
        this.currency = currency;
        this.type = type;

        CreateAccountCommand.accountCreated(name, currency, type);
    }

    // Constructor used by Mapper
    Account(
            Long id,
            UserIdentifier user,
            String name,
            String description,
            String currency,
            String iban,
            String bic,
            String number,
            String type,
            String imageFileToken,
            LocalDate lastTransaction,
            LocalDate firstTransaction,
            double interest,
            Periodicity interestPeriodicity,
            java.util.Set<SavingGoal> savingGoals,
            boolean remove) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.iban = iban;
        this.bic = bic;
        this.number = number;
        this.type = type;
        this.imageFileToken = imageFileToken;
        this.lastTransaction = lastTransaction;
        this.firstTransaction = firstTransaction;
        this.interest = interest;
        this.interestPeriodicity = interestPeriodicity;
        this.savingGoals = Collections.Set(savingGoals);
        this.remove = remove;
    }

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

            RenameAccountCommand.accountRenamed(id, type, name, description, currency);
        }
    }

    public void registerIcon(String fileCode) {
        RegisterAccountIconCommand.iconChanged(id, fileCode, this.imageFileToken);
        this.imageFileToken = fileCode;
    }

    /**
     * Change the interest rate for this account.
     *
     * @param interest the new interest rate
     * @param periodicity the periodicity of the interest rate
     */
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
            ChangeInterestCommand.interestChanged(id, interest, periodicity);
        }
    }

    public void registerSynonym(String synonym) {
        RegisterSynonymCommand.synonymRegistered(id, synonym);
    }

    /**
     * Modify the banking detail information, being account number, IBAN and BIC.
     *
     * @param iban the new IBAN
     * @param bic the new BIC
     * @param number the account number
     */
    public void changeAccount(String iban, String bic, String number) {
        var noChanges = Control.Equal(this.iban, iban)
                .append(this.bic, bic)
                .append(this.number, number)
                .isEqual();

        if (!noChanges) {
            this.iban = iban;
            this.bic = bic;
            this.number = number;
            ChangeAccountCommand.accountChanged(id, iban, bic, number);
        }
    }

    /** Close this account, making it archived and no longer accessible. */
    public void terminate() {
        remove = true;
        TerminateAccountCommand.accountTerminated(id);
    }

    // todo
    //    public Transaction createTransaction(
    //            Account to,
    //            double amount,
    //            Transaction.Type transactionType,
    //            Consumer<Transaction.TransactionBuilder> applier) {
    //        Account destination;
    //        Account source;
    //        switch (transactionType) {
    //            case DEBIT -> {
    //                source = to;
    //                destination = this;
    //            }
    //            case CREDIT, TRANSFER -> {
    //                source = this;
    //                destination = to;
    //            }
    //            default -> throw new IllegalArgumentException();
    //        }
    //
    //        var builder = new Transaction(source, destination, amount).toBuilder();
    //        applier.accept(builder);
    //        return builder.build();
    //    }

    //    /**
    //     * Create a new scheduled transaction with the current account being the source account.
    //     *
    //     * @param name the name of the schedule
    //     * @param schedule the schedule to adhere to
    //     * @param destination the destination account
    //     * @param amount the amount involved in the transactions
    //     * @return the newly created scheduled transaction (not yet persisted)
    //     */
    //    @BusinessMethod
    //    public ScheduledTransaction createSchedule(
    //            String name, Schedule schedule, Account destination, double amount) {
    //        return new ScheduledTransaction(name, schedule, this, destination, amount);
    //    }

    //    /**
    //     * Create a new contract for this account.
    //     *
    //     * @param name the name of the contract
    //     * @param start the start date of the contract
    //     * @param end the end date of the contract
    //     * @return the newly created contract
    //     */
    //    @BusinessMethod
    //    public Contract createContract(
    //            String name, String description, LocalDate start, LocalDate end) {
    //        return new Contract(this, name, description, start, end);
    //    }

    /**
     * Create a new saving goal for the account. This can only be done for accounts of type SAVING
     * or COMBINED_SAVING.
     *
     * @return the newly created saving goal
     */
    public SavingGoal createSavingGoal(String name, BigDecimal goal, LocalDate targetDate) {
        if (!Collections.List("savings", "joined_savings").contains(type.toLowerCase())) {
            throw StatusException.badRequest("Cannot add a savings goal to account " + id
                    + " it is of unsupported type " + type);
        }

        return SavingGoal.create(this, name, goal, targetDate);
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

    public Long getId() {
        return id;
    }

    public UserIdentifier getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrency() {
        return currency;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public String getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public String getImageFileToken() {
        return imageFileToken;
    }

    public LocalDate getLastTransaction() {
        return lastTransaction;
    }

    public LocalDate getFirstTransaction() {
        return firstTransaction;
    }

    public double getInterest() {
        return interest;
    }

    public Periodicity getInterestPeriodicity() {
        return interestPeriodicity;
    }

    public Set<SavingGoal> getSavingGoals() {
        return savingGoals;
    }

    public boolean isRemove() {
        return remove;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static Account create(UserIdentifier user, String name, String currency, String type) {
        return new Account(name, currency, type);
    }
}
