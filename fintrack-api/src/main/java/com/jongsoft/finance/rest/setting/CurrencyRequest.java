package com.jongsoft.finance.rest.setting;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Serdeable.Deserializable
public record CurrencyRequest(
        @NotBlank
        @Size(max = 255)
        String name,
        @NotBlank
        @Size(min = 1, max = 3)
        String code,
        @NotNull
        @Size(min = 1, max = 1)
        String symbol) {
    public char getSymbol() {
        return symbol.charAt(0);
    }
}
