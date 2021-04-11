package com.jongsoft.finance.jpa.transaction.entity;

import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.core.entity.AuditedJpa;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * A TransactionJpa is a part of a transaction journal. Usually a transaction journal contains two transactions, being:
 *
 * <ol>
 *     <li>A debit transaction into one account</li>
 *     <li>A credit transaction into a second account</li>
 * </ol>
 *
 * More advanced examples are split transactions, where you have multiple smaller debit transactions matching one credit transaction.
 */
@Getter
@Entity
@Table(name = "transaction_part")
public class TransactionJpa extends AuditedJpa {

    @ManyToOne
    @JoinColumn(nullable = false)
    private AccountJpa account;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private TransactionJournal journal;

    private double amount;
    private String description;

    public TransactionJpa() {
    }

    @Builder
    protected TransactionJpa(
            Long id,
            Date created,
            Date updated,
            Date deleted,
            AccountJpa account,
            TransactionJournal journal,
            double amount,
            String description) {
        super(id, created, updated, deleted);

        this.account = account;
        this.journal = journal;
        this.amount = amount;
        this.description = description;
    }
}
