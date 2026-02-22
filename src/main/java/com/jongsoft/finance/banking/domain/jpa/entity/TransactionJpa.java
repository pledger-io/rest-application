package com.jongsoft.finance.banking.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * A TransactionJpa is a part of a transaction journal. Usually a transaction journal contains two
 * transactions, being:
 *
 * <ol>
 *   <li>A debit transaction into one account
 *   <li>A credit transaction into a second account
 * </ol>
 *
 * More advanced examples are split transactions, where you have multiple smaller debit transactions
 * matching one credit transaction.
 */
@Entity
@Table(name = "transaction_part")
public class TransactionJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(updatable = false)
    private Date created;

    private Date updated;
    private Date deleted;

    @ManyToOne
    @JoinColumn(nullable = false)
    private AccountJpa account;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private TransactionJournal journal;

    private BigDecimal amount;
    private String description;

    public TransactionJpa() {}

    public TransactionJpa(
            AccountJpa account, TransactionJournal journal, BigDecimal amount, String description) {
        this.account = account;
        this.journal = journal;
        this.amount = amount;
        this.description = description;
    }

    @PreUpdate
    @PrePersist
    void initialize() {
        if (created == null) {
            created = new Date();
        }

        updated = new Date();
    }

    @Override
    public Long getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }

    public Date getDeleted() {
        return deleted;
    }

    public AccountJpa getAccount() {
        return account;
    }

    public TransactionJournal getJournal() {
        return journal;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}
