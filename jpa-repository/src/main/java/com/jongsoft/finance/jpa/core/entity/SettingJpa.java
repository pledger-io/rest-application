package com.jongsoft.finance.jpa.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

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
