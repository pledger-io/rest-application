package com.jongsoft.finance.banking.domain.model;

import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.banking.types.FailureCode;
import com.jongsoft.finance.banking.types.TransactionLinkType;
import com.jongsoft.finance.banking.types.TransactionType;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.List;
import com.jongsoft.lang.collection.Sequence;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@Introspected
public class Transaction implements Serializable {

    private static final Predicate<Part> FROM_PREDICATE = t -> t.amount < 0D;
    private static final Predicate<Part> TO_PREDICATE = t -> t.amount > 0D;

    public enum Type {
        CREDIT("long-arrow-alt-left"),
        DEBIT("long-arrow-alt-right"),
        TRANSFER("exchange-alt");

        private final String style;

        Type(String style) {
            this.style = style;
        }

        public String getStyle() {
            return style;
        }
    }

    @Introspected
    public static class Part implements Serializable {
        private Long id;
        private String description;
        private double amount;
        private Account account;

        private Part(Account account, double amount, String description) {
            this.account = account;
            this.amount = amount;
            this.description = description;
        }

        Part(Long id, String description, double amount, Account account) {
            this.id = id;
            this.description = description;
            this.amount = amount;
            this.account = account;
        }

        public Long getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public double getAmount() {
            return amount;
        }

        public Account getAccount() {
            return account;
        }

        public static Part create(Account account, double amount, String description) {
            return new Part(account, amount, description);
        }
    }

    private Long id;

    private LocalDate date;
    private LocalDate interestDate;
    private LocalDate bookDate;

    private String description;
    private String currency;

    private Map<String, ? extends Classifier> metadata;
    private String importSlug;
    private Sequence<String> tags;

    private List<Part> transactions;
    private FailureCode failureCode;

    private Date created;
    private Date updated;
    private boolean deleted;

    private Transaction(
            Account from, Account to, LocalDate date, double amount, String description) {
        var toAmount = Math.abs(amount);
        var fromAmount = 0 - Math.abs(amount);
        this.currency = from.getCurrency();
        this.description = description;
        this.date = date;

        this.transactions = Collections.List(
                new Part(from, fromAmount, description), new Part(to, toAmount, description));
    }

    Transaction(
            Long id,
            LocalDate date,
            LocalDate interestDate,
            LocalDate bookDate,
            String description,
            String currency,
            Map<String, ? extends Classifier> metadata,
            String importSlug,
            java.util.Set<String> tags,
            java.util.List<Part> transactions,
            FailureCode failureCode,
            Date created,
            Date updated,
            boolean deleted) {
        this.id = id;
        this.date = date;
        this.interestDate = interestDate;
        this.bookDate = bookDate;
        this.description = description;
        this.currency = currency;
        this.metadata = metadata;
        this.importSlug = importSlug;
        this.tags = Collections.List(tags);
        this.transactions = Collections.List(transactions);
        this.failureCode = failureCode;
        this.created = created;
        this.updated = updated;
        this.deleted = deleted;
    }

    public void book(LocalDate date, LocalDate bookDate, LocalDate interestDate) {
        var hasChanged = Control.Equal(date, this.date)
                .append(bookDate, this.bookDate)
                .append(interestDate, this.interestDate)
                .isNotEqual();

        if (hasChanged) {
            this.date = date;
            this.bookDate = bookDate;
            this.interestDate = interestDate;
            ChangeTransactionDatesCommand.transactionDatesChanged(id, date, bookDate, interestDate);
        }
    }

    public void describe(String description) {
        if (Control.Equal(this.description, description).isNotEqual()) {
            this.description = description;
            DescribeTransactionCommand.transactionDescribed(id, description);
        }
    }

    public void changeAmount(double amount, String currency) {
        var hasChanged = Control.Equal(Math.abs(this.computeAmount(this.computeTo())), amount)
                .append(this.currency, currency)
                .isNotEqual();

        if (hasChanged) {
            if (transactions.size() != 2) {
                throw new IllegalStateException(
                        "Transaction amount cannot be changed for split transactions");
            }

            this.currency = currency;
            this.transactions.forEach(
                    t -> t.amount = t.amount < 0 ? 0 - Math.abs(amount) : Math.abs(amount));
            ChangeTransactionAmountCommand.amountChanged(id, BigDecimal.valueOf(amount), currency);
        }
    }

    public void split(List<SplitRecord> split) {
        if (computeFrom().isManaged() && computeTo().isManaged()) {
            throw new IllegalStateException(
                    "Transaction cannot be split when both accounts are your own");
        }

        var notOwn = computeTo().isManaged() ? computeFrom() : computeTo();
        var totalAmount = split.foldLeft(0D, (total, record) -> total + record.amount());

        var isCredit = computeType() == Type.CREDIT;
        if (Math.abs(totalAmount) != Math.abs(computeAmount(notOwn))) {
            var multiplier = isCredit ? -1 : 1;
            var ownPart = this.transactions
                    .filter(t -> !t.getAccount().equals(notOwn))
                    .head();
            ownPart.amount = multiplier * split.map(SplitRecord::amount).reduce(Double::sum);
        }

        var splitParts = split.map(record -> new Part(
                notOwn,
                isCredit ? Math.abs(record.amount()) : -Math.abs(record.amount()),
                record.description()));

        this.transactions =
                this.transactions.reject(t -> t.getAccount().equals(notOwn)).union(splitParts);

        SplitTransactionCommand.transactionSplit(id, transactions.stream().toList());
    }

    public void changeAccount(boolean isFromAccount, Account account) {
        var original = isFromAccount ? computeFrom() : computeTo();
        if (!Objects.equals(original, account)) {

            var other = isFromAccount ? computeTo() : computeFrom();
            if (other.equals(account)) {
                failureCode = FailureCode.FROM_TO_SAME;
            }

            transactions.filter(isFromAccount ? FROM_PREDICATE : TO_PREDICATE).forEach(t -> {
                t.account = account;
                ChangeTransactionPartAccount.transactionPartAccountChanged(
                        t.getId(), account.getId());
            });
        }
    }

    public void link(TransactionLinkType type, Long id) {
        LinkTransactionCommand.linkCreated(this.id, type, id);
    }

    public void tag(Sequence<String> tags) {
        if (!Objects.equals(this.tags, tags)) {
            this.tags = tags;
            TagTransactionCommand.tagCreated(id, tags);
        }
    }

    public void registerFailure(FailureCode failureCode) {
        this.failureCode = failureCode;
        RegisterFailureCommand.registerFailure(id, failureCode);
    }

    public void register() {
        if (this.created != null) {
            throw new IllegalStateException(
                    "Cannot register transaction it already exists in the system.");
        }

        if (computeFrom().equals(computeTo())) {
            failureCode = FailureCode.FROM_TO_SAME;
        } else if (transactions.stream().mapToDouble(Part::getAmount).sum() != 0) {
            failureCode = FailureCode.AMOUNT_NOT_NULL;
        }

        CreateTransactionCommand.transactionCreated(
                this.date,
                this.description,
                TransactionType.valueOf(computeType().name()),
                this.failureCode,
                this.currency,
                computeFrom().getId(),
                computeTo().getId(),
                BigDecimal.valueOf(computeAmount(computeTo())));
    }

    public void delete() {
        if (id == null) {
            throw new IllegalStateException("Cannot delete a transaction not yet persisted.");
        }

        DeleteTransactionCommand.transactionDeleted(id);
    }

    /**
     * Calculate the amount being transferred to the specified account in this transaction.
     *
     * @param account the account to calculate the transfer for
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
                .getOrThrow(() -> new IllegalStateException("Transaction has no from account."));
    }

    public Account computeTo() {
        return transactions
                .first(TO_PREDICATE)
                .map(Part::getAccount)
                .getOrThrow(() -> new IllegalStateException("Transaction has no to account."));
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

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDate getInterestDate() {
        return interestDate;
    }

    public LocalDate getBookDate() {
        return bookDate;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrency() {
        return currency;
    }

    public Map<String, ? extends Classifier> getMetadata() {
        return metadata;
    }

    public String getImportSlug() {
        return importSlug;
    }

    public Sequence<String> getTags() {
        return tags;
    }

    public List<Part> getTransactions() {
        return transactions;
    }

    public FailureCode getFailureCode() {
        return failureCode;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String toString() {
        return "Transaction from "
                + computeFrom()
                + " to "
                + computeTo()
                + " with amount "
                + computeAmount(computeFrom());
    }

    public static Transaction create(
            Account from, Account to, LocalDate date, double amount, String description) {
        return new Transaction(from, to, date, amount, description);
    }
}
