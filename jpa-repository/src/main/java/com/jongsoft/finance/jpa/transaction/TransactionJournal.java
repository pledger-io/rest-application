package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.core.FailureCode;
import com.jongsoft.finance.core.TransactionType;
import com.jongsoft.finance.jpa.core.entity.AuditedJpa;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;

import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Getter
@Table(name = "transaction_journal")
public class TransactionJournal extends AuditedJpa {

    @Column(name = "t_date", nullable = false, columnDefinition = "DATE")
    private LocalDate date;

    @Column(name = "interest_date", columnDefinition = "DATE")
    private LocalDate interestDate;

    @Column(name = "book_date", columnDefinition = "DATE")
    private LocalDate bookDate;

    private String description;

    @Enumerated(value = EnumType.STRING)
    private TransactionType type;

    @Enumerated(value = EnumType.STRING)
    private FailureCode failureCode;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private UserAccountJpa user;

    @ManyToOne
    @JoinColumn
    private CurrencyJpa currency;

    @JoinTable(
            name = "transaction_tag",
            joinColumns = @JoinColumn(name = "transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<TagJpa> tags;

    @Where(clause = "deleted is null")
    @OneToMany(mappedBy = "journal", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<TransactionJpa> transactions;

    @OneToMany(mappedBy = "journal", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<TransactionMetaJpa> metadata;

    public TransactionJournal() {
        super();
    }

    @Builder
    protected TransactionJournal(
            Long id,
            Date created,
            Date updated,
            Date deleted,
            LocalDate date,
            LocalDate bookDate,
            LocalDate interestDate,
            String description,
            TransactionType type,
            FailureCode failureCode,
            UserAccountJpa user,
            Set<TransactionMetaJpa> metadata,
            CurrencyJpa currency,
            Set<TagJpa> tags,
            Set<TransactionJpa> transactions) {
        super(id, created, updated, deleted);

        this.date = date;
        this.bookDate = bookDate;
        this.interestDate = interestDate;
        this.description = description;
        this.type = type;
        this.failureCode = failureCode;
        this.user = user;
        this.metadata = Optional.ofNullable(metadata).orElseGet(HashSet::new);
        this.currency = currency;
        this.tags = tags;
        this.transactions = transactions;
    }
}
