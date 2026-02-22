package com.jongsoft.finance.core.domain.jpa.entity;

import com.jongsoft.finance.core.domain.model.SettingType;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

@Entity
@Introspected
@Table(name = "setting")
public class SettingJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private SettingType type;

    @Column(name = "setting_val")
    private String value;

    public SettingJpa() {
        super();
    }

    protected SettingJpa(Long id, String name, SettingType type, String value) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SettingType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
