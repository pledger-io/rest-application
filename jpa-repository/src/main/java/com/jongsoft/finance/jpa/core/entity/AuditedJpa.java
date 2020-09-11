package com.jongsoft.finance.jpa.core.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class AuditedJpa extends EntityJpa {

    @Column(updatable = false)
    private Date created;
    private Date updated;
    private Date deleted;

    public AuditedJpa() {
    }

    protected AuditedJpa(Long id, Date created, Date updated, Date deleted) {
        super(id);

        this.created = created;
        this.updated = updated;
        this.deleted = deleted;
    }

    @PreUpdate
    @PrePersist
    void initialize() {
        if (created == null) {
            created = new Date();
        }

        updated = new Date();
    }
}
