package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;

import jakarta.persistence.*;

import lombok.Getter;

@Entity
@Getter
@Table(name = "transaction_journal_meta")
public class TransactionMetaJpa extends EntityJpa {

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
}
