package com.jongsoft.finance.banking.domain.jpa.entity;

import com.jongsoft.finance.banking.types.FailureCode;
import com.jongsoft.finance.banking.types.TransactionType;
import com.jongsoft.finance.core.domain.jpa.entity.CurrencyJpa;
import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "transaction_journal")
public class TransactionJournal implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(updatable = false)
    private Date created;

    private Date updated;
    private Date deleted;

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

    @SQLRestriction("deleted is null")
    @OneToMany(mappedBy = "journal", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<TransactionJpa> transactions;

    @OneToMany(mappedBy = "journal", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<TransactionMetaJpa> metadata;

    public TransactionJournal() {
        super();
    }

    public TransactionJournal(
            LocalDate date,
            LocalDate bookDate,
            LocalDate interestDate,
            String description,
            TransactionType type,
            FailureCode failureCode,
            UserAccountJpa user,
            CurrencyJpa currency) {
        this.date = date;
        this.bookDate = bookDate;
        this.interestDate = interestDate;
        this.description = description;
        this.type = type;
        this.failureCode = failureCode;
        this.user = user;
        this.metadata = new HashSet<>();
        this.transactions = new HashSet<>();
        this.currency = currency;
    }

    @PrePersist
    void prePersist() {
        created = new Date();
        updated = created;
    }

    @PreUpdate
    void preUpdate() {
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

    public TransactionType getType() {
        return type;
    }

    public FailureCode getFailureCode() {
        return failureCode;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public CurrencyJpa getCurrency() {
        return currency;
    }

    public Set<TagJpa> getTags() {
        return tags;
    }

    public Set<TransactionJpa> getTransactions() {
        return transactions;
    }

    public Set<TransactionMetaJpa> getMetadata() {
        return metadata;
    }
}
