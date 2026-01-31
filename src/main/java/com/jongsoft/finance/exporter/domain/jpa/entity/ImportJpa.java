package com.jongsoft.finance.exporter.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Introspected
@Table(name = "import")
public class ImportJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private Date created;
    private Date finished;
    private String slug;
    private boolean archived;

    @Column
    private String fileCode;

    @ManyToOne
    @JoinColumn
    private ImportConfig config;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    public ImportJpa(
            String slug,
            String fileCode,
            ImportConfig config,
            UserAccountJpa user,
            boolean archived) {
        this.slug = slug;
        this.fileCode = fileCode;
        this.config = config;
        this.user = user;
        this.archived = archived;
    }

    protected ImportJpa() {}

    @PrePersist
    void initialize() {
        if (created == null) {
            created = new Date();
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public Date getFinished() {
        return finished;
    }

    public String getSlug() {
        return slug;
    }

    public boolean isArchived() {
        return archived;
    }

    public String getFileCode() {
        return fileCode;
    }

    public ImportConfig getConfig() {
        return config;
    }

    public UserAccountJpa getUser() {
        return user;
    }
}
