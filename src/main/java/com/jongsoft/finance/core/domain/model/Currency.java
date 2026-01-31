package com.jongsoft.finance.core.domain.model;

import com.jongsoft.finance.core.domain.commands.ChangeCurrencyPropertyCommand;
import com.jongsoft.finance.core.domain.commands.CreateCurrencyCommand;
import com.jongsoft.finance.core.domain.commands.CurrencyCommandType;
import com.jongsoft.finance.core.domain.commands.RenameCurrencyCommand;
import com.jongsoft.lang.Control;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Currency {

    private Long id;
    private String name;
    private String code;
    private char symbol;
    private int decimalPlaces;
    private boolean enabled;

    private Currency(String name, String code, char symbol) {
        this.name = name;
        this.code = code;
        this.symbol = symbol;
        this.decimalPlaces = 2;
        this.enabled = true;

        CreateCurrencyCommand.currencyCreated(name, symbol, code);
    }

    Currency(Long id, String name, String code, char symbol, int decimalPlaces, boolean enabled) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
        this.enabled = enabled;
    }

    public void rename(String name, String code, char symbol) {
        var changed = Control.Equal(this.name, name)
                .append(this.code, code)
                .append(this.symbol, symbol)
                .isNotEqual();

        if (changed) {
            this.name = name;
            this.code = code;
            this.symbol = symbol;
            RenameCurrencyCommand.currencyRenamed(id, name, symbol, code);
        }
    }

    public void disable() {
        if (enabled) {
            enabled = false;
            ChangeCurrencyPropertyCommand.currencyPropertyChanged(
                    code, false, CurrencyCommandType.ENABLED);
        }
    }

    public void enable() {
        if (!enabled) {
            enabled = true;
            ChangeCurrencyPropertyCommand.currencyPropertyChanged(
                    code, true, CurrencyCommandType.ENABLED);
        }
    }

    public void accuracy(int decimalPlaces) {
        if (decimalPlaces != this.decimalPlaces) {
            this.decimalPlaces = decimalPlaces;
            ChangeCurrencyPropertyCommand.currencyPropertyChanged(
                    code, decimalPlaces, CurrencyCommandType.DECIMAL_PLACES);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public char getSymbol() {
        return symbol;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static Currency create(String name, String code, char symbol) {
        return new Currency(name, code, symbol);
    }
}
