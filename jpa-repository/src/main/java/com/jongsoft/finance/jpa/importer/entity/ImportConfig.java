package com.jongsoft.finance.jpa.importer.entity;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "import_config")
public class ImportConfig extends EntityJpa {

    private String name;

    @Column private String fileCode;

    @Column private String type;

    @ManyToOne @JoinColumn private UserAccountJpa user;

    @Builder
    private ImportConfig(String name, String fileCode, String type, UserAccountJpa user) {
        this.name = name;
        this.fileCode = fileCode;
        this.user = user;
        this.type = type;
    }

    protected ImportConfig() {}
}
