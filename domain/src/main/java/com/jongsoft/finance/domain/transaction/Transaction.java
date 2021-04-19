package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.FailureCode;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.transaction.*;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.List;
import com.jongsoft.lang.collection.Sequence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.function.Predicate;

@Getter
@Aggregate
@AllArgsConstructor
@Builder(toBuilder = true)
public class Transaction implements AggregateBase, Serializable {

    private static final Predicate<Part> FROM_PREDICATE = t -> t.amount < 0D;
    private static final Predicate<Part> TO_PREDICATE = t -> t.amount > 0D;

    @Getter
    public enum Type {
        CREDIT("long-arrow-alt-left"),
        DEBIT("long-arrow-alt-right"),
        TRANSFER("exchange-alt");

        private final String style;

        Type(String style) {
            this.style = style;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Part implements AggregateBase, Serializable {

        private Long id;
        private String description;
        private double amount;
        private Account account;

        public Part(Account account, double amount) {
            this.account = account;
            this.amount = amount;
        }

    }

    private Long id;
    private transient UserAccount user;

    private LocalDate date;
    private LocalDate interestDate;
    private LocalDate bookDate;

    private String description;
    private String currency;
    private String category;
    private String budget;
    private String contract;
    private String importSlug;
    private Sequence<String> tags;

    private Sequence<Part> transactions;
    private FailureCode failureCode;

    private Date created;
    private Date updated;

    public Transaction(UserAccount user, Account from, Account to, double amount) {
        this.user = user;

        var toAmount = Math.abs(amount);
        var fromAmount = 0 - Math.abs(amount);

        this.transactions = Collections.List(
                Part.builder().account(from).amount(fromAmount).build(),
                Part.builder().account(to).amount(toAmount).build()
        );
    }

    @BusinessMethod
    public void book(LocalDate date, LocalDate bookDate, LocalDate interestDate) {
        var hasChanged = Control.Equal(date, this.date)
                .append(bookDate, this.bookDate)
                .append(interestDate, this.interestDate)
                .isNotEqual();

        if (hasChanged) {
            this.date = date;
            this.bookDate = bookDate;
            this.interestDate = interestDate;

            EventBus.getBus().send(
                    new ChangeTransactionDatesCommand(id, date, bookDate, interestDate));
        }
    }

    @BusinessMethod
    public void describe(String description) {
        if (Control.Equal(this.description, description).isNotEqual()) {
            this.description = description;
            EventBus.getBus().send(
                    new DescribeTransactionCommand(id, description));
        }
    }

    @BusinessMethod
    public void changeAmount(double amount, String currency) {
        var hasChanged = Control.Equal(Math.abs(this.computeAmount(this.computeTo())), amount)
                .append(this.currency, currency)
                .isNotEqual();

        if (hasChanged) {
            if (transactions.size() != 2) {
                throw new IllegalStateException("Transaction amount cannot be changed for split transactions");
            }

            this.currency = currency;
            this.transactions.forEach(t -> t.amount = t.amount < 0 ? 0 - Math.abs(amount) : Math.abs(amount));
            EventBus.getBus().send(
                    new ChangeTransactionAmountCommand(id, BigDecimal.valueOf(amount), currency));
        }
    }

    @BusinessMethod
    public void split(List<SplitRecord> split) {
        if (computeFrom().isManaged() && computeTo().isManaged()) {
            throw new IllegalStateException("Transaction cannot be split when both accounts are your own");
        }

        var notOwn = computeTo().isManaged() ? computeFrom() : computeTo();
        var totalAmount = split.foldLeft(0D, (total, record) -> total + record.amount());

        var isCredit = computeType() == Type.CREDIT;
        if (Math.abs(totalAmount) != Math.abs(computeAmount(notOwn))) {
            var multiplier = isCredit ? -1 : 1;
            var ownPart = this.transactions.filter(t -> !t.getAccount().equals(notOwn)).head();
            ownPart.amount = multiplier * split
                    .map(SplitRecord::amount)
                    .reduce(Double::sum);
        }

        var splitParts = split.map(record ->
                Part.builder()
                        .account(notOwn)
                        .description(record.description())
                        .amount(isCredit ? Math.abs(record.amount()) : -Math.abs(record.amount()))
                        .build());

        this.transactions = this.transactions
                .reject(t -> t.getAccount().equals(notOwn))
                .union(splitParts);

        EventBus.getBus().send(new SplitTransactionCommand(id, transactions));
    }

    @BusinessMethod
    public void changeAccount(boolean isFromAccount, Account account) {
        var original = isFromAccount ? computeFrom() : computeTo();
        if (!Objects.equals(original, account)) {

            var other = isFromAccount ? computeTo() : computeFrom();
            if (other.equals(account)) {
                failureCode = FailureCode.FROM_TO_SAME;
            }

            transactions
                    .filter(isFromAccount ? FROM_PREDICATE : TO_PREDICATE)
                    .forEach(t -> {
                        t.account = account;
                        EventBus.getBus().send(new ChangeTransactionPartAccount(t.getId(), account.getId()));
                    });
        }
    }

    @BusinessMethod
    public void linkToCategory(String label) {
        if (!Objects.equals(this.category, label)) {
            this.category = label;
            EventBus.getBus().send(
                    new LinkTransactionCommand(id, LinkTransactionCommand.LinkType.CATEGORY, category));
        }
    }

    @BusinessMethod
    public void linkToBudget(String budget) {
        if (budget != null && !Objects.equals(this.budget, budget)) {
            this.budget = budget;
            EventBus.getBus().send(
                    new LinkTransactionCommand(id, LinkTransactionCommand.LinkType.EXPENSE, budget));
        }
    }

    @BusinessMethod
    public void linkToContract(String contract) {
        if (!Objects.equals(this.contract, contract)) {
            this.contract = contract;
            EventBus.getBus().send(
                    new LinkTransactionCommand(id, LinkTransactionCommand.LinkType.CONTRACT, contract));
        }
    }

    @BusinessMethod
    public void tag(Sequence<String> tags) {
        if (!Objects.equals(this.tags, tags)) {
            this.tags = tags;
            EventBus.getBus().send(new TagTransactionCommand(id, tags));
        }
    }

    @BusinessMethod
    public void registerFailure(FailureCode failureCode) {
        this.failureCode = failureCode;
        EventBus.getBus().send(new RegisterFailureCommand(this.id, this.failureCode));
    }

    @BusinessMethod
    public void linkToImport(String slug) {
        if (this.importSlug != null) {
            throw new IllegalStateException("Cannot link transaction to an import, it's already linked.");
        }

        this.importSlug = slug;
    }

    @BusinessMethod
    public void register() {
        if (this.created != null) {
            throw new IllegalStateException("Cannot register transaction it already exists in the system.");
        }

        if (computeFrom().equals(computeTo())) {
            failureCode = FailureCode.FROM_TO_SAME;
        } else if (transactions.stream().mapToDouble(Part::getAmount).sum() != 0) {
            failureCode = FailureCode.AMOUNT_NOT_NULL;
        }

        EventBus.getBus().send(new CreateTransactionCommand(this));
    }

    @BusinessMethod
    public void delete() {
        if (id == null) {
            throw new IllegalStateException("Cannot delete a transaction not yet persisted.");
        }

        EventBus.getBus().send(new DeleteTransactionCommand(id));
    }

    /**
     * Calculate the amount being transferred to the specified account in this transaction.
     *
     * @param account   the account to calculate the transfer for
     * @return the calculated amount
     */
    public double computeAmount(Account account) {
        return transactions
                .filter(t -> Objects.equals(t.getAccount(), account))
                .map(Part::getAmount)
                .reduce(Double::sum);
    }

    public Account computeFrom() {
        return transactions
                .first(FROM_PREDICATE)
                .map(Part::getAccount)
                .get();
    }

    public Account computeTo() {
        return transactions
                .first(TO_PREDICATE)
                .map(Part::getAccount)
                .get();
    }

    public Account computeCounter(Account account) {
        return transactions
                .first(t -> !Objects.equals(t.getAccount(), account))
                .map(Part::getAccount)
                .get();
    }

    public Transaction.Type computeType() {
        var fromManaged = computeFrom().isManaged();
        var toManaged = computeTo().isManaged();

        if (fromManaged && toManaged) {
            return Type.TRANSFER;
        } else if (fromManaged) {
            return Type.CREDIT;
        } else {
            return Type.DEBIT;
        }
    }

    public boolean isSplit() {
        return transactions.size() > 2;
    }

    public boolean isDebit(Account account) {
        return computeAmount(account) > 0;
    }

    @Override
    public String toString() {
        return "Transaction from " + computeFrom() + " to " + computeTo() + " with amount " + computeAmount(computeFrom());
    }

}
