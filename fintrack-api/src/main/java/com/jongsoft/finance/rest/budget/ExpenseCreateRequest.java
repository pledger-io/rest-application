package com.jongsoft.finance.rest.budget;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
