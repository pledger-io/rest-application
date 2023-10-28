package com.jongsoft.finance.rest.budget;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Serdeable.Deserializable
class ExpenseCreateRequest {

    @NotNull
    @NotBlank
    private String name;

    @Min(0)
    private double lowerBound;

    @Min(1)
    private double upperBound;

    public String getName() {
        return name;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }
}
