package com.jongsoft.finance.banking.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;

import jakarta.persistence.*;

@Entity
@Table(name = "transaction_journal_meta")
public class TransactionMetaJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    private TransactionJournal journal;

    private String relationType;
    private Long entityId;

    protected TransactionMetaJpa() {}

    public TransactionMetaJpa(TransactionJournal journal, String relationType, Long entityId) {
        this.journal = journal;
        this.relationType = relationType;
        this.entityId = entityId;
    }

    @Override
    public Long getId() {
        return id;
    }

    public TransactionJournal getJournal() {
        return journal;
    }

    public String getRelationType() {
        return relationType;
    }

    public Long getEntityId() {
        return entityId;
    }
}
