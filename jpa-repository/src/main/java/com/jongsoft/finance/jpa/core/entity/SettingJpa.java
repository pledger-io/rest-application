package com.jongsoft.finance.jpa.core.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.jongsoft.finance.core.SettingType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "setting")
public class SettingJpa extends EntityJpa {

    private String name;

    @Enumerated(EnumType.STRING)
    private SettingType type;

    private String value;

    public SettingJpa() {
        super();
    }

    @Builder
    protected SettingJpa(Long id, String name, SettingType type, String value) {
        super(id);

        this.name = name;
        this.type = type;
        this.value = value;
    }
}
