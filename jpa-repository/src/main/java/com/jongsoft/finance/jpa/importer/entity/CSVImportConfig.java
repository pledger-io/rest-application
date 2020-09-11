package com.jongsoft.finance.jpa.importer.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
