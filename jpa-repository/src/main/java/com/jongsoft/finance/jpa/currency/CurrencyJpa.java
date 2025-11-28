package com.jongsoft.finance.jpa.currency;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "currency")
public class CurrencyJpa extends EntityJpa {

    private String name;
    private char symbol;
    private String code;

    private int decimalPlaces;
    private boolean enabled;

    private boolean archived;

    public CurrencyJpa() {
        super();
    }

    @Builder
    protected CurrencyJpa(
            Long id,
            String name,
            char symbol,
            String code,
            int decimalPlaces,
            boolean enabled,
            boolean archived) {
        super(id);

        this.name = name;
        this.symbol = symbol;
        this.code = code;
        this.decimalPlaces = decimalPlaces;
        this.enabled = enabled;
        this.archived = archived;
    }
}
