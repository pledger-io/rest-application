package com.jongsoft.finance.exporter.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

@Entity
@Introspected
@Table(name = "import_config")
public class ImportConfig implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;

    @Column
    private String fileCode;

    @Column
    private String type;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    public ImportConfig(String name, String fileCode, String type, UserAccountJpa user) {
        this.name = name;
        this.fileCode = fileCode;
        this.user = user;
        this.type = type;
    }

    protected ImportConfig() {}

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFileCode() {
        return fileCode;
    }

    public String getType() {
        return type;
    }

    public UserAccountJpa getUser() {
        return user;
    }
}
