package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.core.Currency;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class CurrencyResponse {

    private final Currency wrapped;

    public CurrencyResponse(Currency wrapped) {
        this.wrapped = wrapped;
    }

    public String getName() {
        return wrapped.getName();
    }

    public String getCode() {
        return wrapped.getCode();
    }

    public char getSymbol() {
        return wrapped.getSymbol();
    }

    public int getNumberDecimals() {
        return wrapped.getDecimalPlaces();
    }

    public boolean isEnabled() {
        return wrapped.isEnabled();
    }

}
