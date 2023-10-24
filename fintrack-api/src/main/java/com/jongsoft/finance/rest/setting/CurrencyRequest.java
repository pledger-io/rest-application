package com.jongsoft.finance.rest.setting;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Serdeable.Deserializable
public class CurrencyRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(min = 1, max = 3)
    private String code;

    @NotBlank
    private char symbol;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public char getSymbol() {
        return symbol;
    }

}
