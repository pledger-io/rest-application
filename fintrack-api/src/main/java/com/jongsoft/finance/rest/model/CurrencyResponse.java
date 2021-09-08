package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.core.Currency;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;

@Introspected
public class CurrencyResponse {

    private final Currency wrapped;

    public CurrencyResponse(Currency wrapped) {
        this.wrapped = wrapped;
    }

    @Schema(description = "The name of the currency", example = "United States dollar")
    public String getName() {
        return wrapped.getName();
    }

    @Schema(description = "The ISO code of the currency", example = "USD")
    public String getCode() {
        return wrapped.getCode();
    }

    @Schema(description = "The currency symbol", example = "$")
    public char getSymbol() {
        return wrapped.getSymbol();
    }

    @Schema(description = "The default amount of decimal places for this currency", example = "2")
    public int getNumberDecimals() {
        return wrapped.getDecimalPlaces();
    }

    @Schema(description = "Indication if the currency is enabled for the application")
    public boolean isEnabled() {
        return wrapped.isEnabled();
    }

}
