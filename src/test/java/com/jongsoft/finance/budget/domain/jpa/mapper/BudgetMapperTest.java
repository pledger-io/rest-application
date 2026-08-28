package com.jongsoft.finance.budget.domain.jpa.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jongsoft.finance.budget.domain.jpa.entity.BudgetJpa;
import com.jongsoft.finance.budget.domain.jpa.entity.ExpenseJpa;
import com.jongsoft.finance.budget.domain.jpa.entity.ExpensePeriodJpa;
import com.jongsoft.finance.budget.domain.model.Budget;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

@DisplayName("Budget mapper")
class BudgetMapperTest {

    private final BudgetMapper mapper = new BudgetMapper() {
        @Override
        public Budget toDomain(BudgetJpa entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Budget.Expense toDomain(ExpensePeriodJpa entity) {
            return new Budget.Expense(
                    entity.getExpense().getId(),
                    entity.getExpense().getName(),
                    entity.getUpperBound().doubleValue());
        }
    };

    @Test
    @DisplayName("Map expenses in alphabetical order")
    void expensesList_orderedByName() {
        var savings = expense(2, "Savings");
        var groceries = expense(1, "Groceries");
        var expenses = new LinkedHashSet<>(List.of(savings, groceries));

        assertThat(mapper.expensesList(expenses))
                .extracting(Budget.Expense::getName)
                .containsExactly("Groceries", "Savings");
    }

    @Test
    @DisplayName("Use expense identifier to order duplicate names")
    void expensesList_duplicateNamesOrderedByIdentifier() {
        var second = expense(2, "Groceries");
        var first = expense(1, "Groceries");
        var expenses = new LinkedHashSet<>(List.of(second, first));

        assertThat(mapper.expensesList(expenses))
                .extracting(Budget.Expense::getId)
                .containsExactly(1L, 2L);
    }

    private ExpensePeriodJpa expense(long id, String name) {
        var expense = mock(ExpenseJpa.class);
        when(expense.getId()).thenReturn(id);
        when(expense.getName()).thenReturn(name);

        return new ExpensePeriodJpa(
                BigDecimal.valueOf(99.99), BigDecimal.valueOf(100), expense, null);
    }
}
