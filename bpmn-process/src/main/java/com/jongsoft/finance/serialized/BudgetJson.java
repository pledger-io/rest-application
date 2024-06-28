package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.user.Budget;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.jsonschema.JsonSchema;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Serdeable
@JsonSchema(title = "Budget", description = "Budget for a user", uri = "/budget")
public class BudgetJson implements Serializable {

    @Data
    @Builder
    @Serdeable
    @JsonSchema(title = "Expense", description = "Expense for a budget", uri = "/budget-expense")
    public static class ExpenseJson implements Serializable {
        @NonNull
        private String name;
        private double lowerBound;
        private double upperBound;
    }

    /**
     * Start date of the budget, in ISO 8601 format.
     */
    @NonNull
    private LocalDate start;
    private LocalDate end;

    /**
     * Expected income for the budget.
     */
    private double expectedIncome;
    /**
     * List of expenses for the budget.
     */
    @NonNull
    private List<ExpenseJson> expenses;

    public static BudgetJson fromDomain(Budget budget) {
        return BudgetJson.builder()
                .start(budget.getStart())
                .end(budget.getEnd())
                .expectedIncome(budget.getExpectedIncome())
                .expenses(budget.getExpenses()
                        .stream()
                        .map(e -> ExpenseJson.builder()
                                .name(e.getName())
                                .lowerBound(e.getLowerBound())
                                .upperBound(e.getUpperBound())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

}
