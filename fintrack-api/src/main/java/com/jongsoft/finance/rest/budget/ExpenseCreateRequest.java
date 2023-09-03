package com.jongsoft.finance.rest.budget;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Introspected
@NoArgsConstructor
class ExpenseCreateRequest {

    @NotNull
    @NotBlank
    private String name;

    @Min(0)
    private double lowerBound;

    @Min(1)
    private double upperBound;

    @Builder
    ExpenseCreateRequest(String name, double lowerBound, double upperBound) {
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

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
