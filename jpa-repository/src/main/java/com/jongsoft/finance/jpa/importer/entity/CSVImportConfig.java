package com.jongsoft.finance.jpa.importer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "import_config")
public class CSVImportConfig extends EntityJpa {

    private String name;

    @Column
    private String fileCode;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    @Builder
    private CSVImportConfig(String name, String fileCode, UserAccountJpa user) {
        this.name = name;
        this.fileCode = fileCode;
        this.user = user;
    }

    protected CSVImportConfig() {
    }
}
