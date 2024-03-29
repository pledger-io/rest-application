package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.user.Budget;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@Serdeable
public class BudgetJson implements Serializable {

    @Getter
    @Setter
    @Builder
    @Serdeable
    public static class ExpenseJson implements Serializable {
        private String name;
        private double lowerBound;
        private double upperBound;
    }

    private LocalDate start;
    private LocalDate end;

    private double expectedIncome;
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
