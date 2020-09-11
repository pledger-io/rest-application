package com.jongsoft.finance.jpa.importer.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.transaction.entity.TransactionJournal;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "import")
public class ImportJpa extends EntityJpa {

    private Date created;
    private Date finished;
    private String slug;

    @Column
    private String fileCode;

    @ManyToOne
    @JoinColumn
    private CSVImportConfig config;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    @OneToMany(mappedBy = "batchImport")
    private List<TransactionJournal> transactions;

    @Builder
    private ImportJpa(
            Date created,
            Date finished,
            String slug,
            String fileCode,
            CSVImportConfig config,
            UserAccountJpa user,
            List<TransactionJournal> transactions) {
        this.created = created;
        this.finished = finished;
        this.slug = slug;
        this.fileCode = fileCode;
        this.config = config;
        this.user = user;
        this.transactions = transactions;
    }

    protected ImportJpa() {
    }

    @PrePersist
    void initialize() {
        if (created == null) {
            created = new Date();
        }
    }

}
