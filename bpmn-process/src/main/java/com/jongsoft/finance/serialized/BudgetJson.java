package com.jongsoft.finance.serialized;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.jongsoft.finance.domain.user.Budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetJson implements Serializable {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
