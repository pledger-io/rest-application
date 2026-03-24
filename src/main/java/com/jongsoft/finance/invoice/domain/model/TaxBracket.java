package com.jongsoft.finance.invoice.domain.model;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;
import java.math.BigDecimal;

@Introspected
public class TaxBracket implements Serializable {

    private Long id;
    private String name;
    private BigDecimal rate;

    // Used by the Mapper strategy
    TaxBracket(Long id, String name, BigDecimal rate) {
        this.id = id;
        this.name = name;
        this.rate = rate;
    }

    private TaxBracket(String name, BigDecimal rate) {
        this.name = name;
        this.rate = rate;
        com.jongsoft.finance.invoice.domain.commands.CreateTaxBracketCommand.taxBracketCreated(
                name, rate);
    }

    public void update(String name, BigDecimal rate) {
        this.name = name;
        this.rate = rate;
        com.jongsoft.finance.invoice.domain.commands.UpdateTaxBracketCommand.taxBracketUpdated(
                id, name, rate);
    }

    public void delete() {
        com.jongsoft.finance.invoice.domain.commands.DeleteTaxBracketCommand.taxBracketDeleted(id);
    }

    public static TaxBracket create(String name, BigDecimal rate) {
        return new TaxBracket(name, rate);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
