package com.jongsoft.finance.core.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

@Entity
@Introspected
@Table(name = "currency")
public class CurrencyJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;
    private char symbol;
    private String code;

    private int decimalPlaces;
    private boolean enabled;

    private boolean archived;

    public CurrencyJpa() {
        super();
    }

    public CurrencyJpa(
            Long id,
            String name,
            char symbol,
            String code,
            int decimalPlaces,
            boolean enabled,
            boolean archived) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.code = code;
        this.decimalPlaces = decimalPlaces;
        this.enabled = enabled;
        this.archived = archived;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public char getSymbol() {
        return symbol;
    }

    public String getCode() {
        return code;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isArchived() {
        return archived;
    }
}
