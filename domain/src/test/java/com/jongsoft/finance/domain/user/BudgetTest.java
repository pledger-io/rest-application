package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.budget.CloseBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateExpenseCommand;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BudgetTest {

    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setup() {
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(eventPublisher);
    }

    @Test
    void createExpense() {
        final Budget budget = Budget.builder()
                .id(1L)
                .start(LocalDate.of(2019, 1, 1))
                .expectedIncome(2300)
                .expenses(Collections.List())
                .build();

        budget.createExpense("Grocery", 200, 400);

        assertThat(budget.getExpenses()).hasSize(1);
        assertThat(budget.getExpenses().get(0).getName()).isEqualTo("Grocery");
        assertThat(budget.getExpenses().get(0).getLowerBound()).isEqualTo(200D);
        assertThat(budget.getExpenses().get(0).getUpperBound()).isEqualTo(400D);

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateExpenseCommand.class));
    }

    @Test
    void createExpense_exceedsLimit() {
        final Budget budget = Budget.builder()
                .id(1L)
                .start(LocalDate.of(2019, 1, 1))
                .expectedIncome(50)
                .expenses(Collections.List())
                .build();

        var thrown = assertThrows(StatusException.class, () -> budget.createExpense("Grocery", 200, 400));

        assertThat(thrown.getMessage()).isEqualTo("Expected expenses exceeds the expected income.");
    }

    @Test
    void createExpense_alreadyClosed() {
        final Budget budget = Budget.builder()
                .id(1L)
                .start(LocalDate.of(2019, 1, 1))
                .end(LocalDate.of(2019, 1, 31))
                .expectedIncome(50)
                .expenses(Collections.List())
                .build();

        var thrown = assertThrows(StatusException.class, () -> budget.createExpense("Grocery", 200, 400));

        assertThat(thrown.getMessage()).isEqualTo("Cannot add expense to an already closed budget period.");
    }

    @Test
    void indexBudget() {
        final Budget budget = Budget.builder()
                .id(1L)
                .start(LocalDate.of(2019, 1, 1))
                .expectedIncome(2300)
                .build();

        budget.new Expense(1L, "Grocery", 300);
        budget.new Expense(2L, "Mortgage", 750);

        Budget indexedBudget = budget.indexBudget(LocalDate.of(2019, 3, 1), 2550);

        assertThat(indexedBudget.getExpectedIncome()).isEqualTo(2550D);
        assertThat(indexedBudget.getExpenses()).hasSize(2);

        assertThat(indexedBudget.getExpenses().get(0).getName()).isEqualTo("Grocery");
        assertThat(indexedBudget.getExpenses().get(0).getLowerBound()).isEqualTo(332.99);
        assertThat(indexedBudget.getExpenses().get(0).getUpperBound()).isEqualTo(333.0);
        assertThat(indexedBudget.getExpenses().get(1).getName()).isEqualTo("Mortgage");
        assertThat(indexedBudget.getExpenses().get(1).getLowerBound()).isEqualTo(831.99);
        assertThat(indexedBudget.getExpenses().get(1).getUpperBound()).isEqualTo(832);
        assertThat(budget.getEnd()).isEqualTo(LocalDate.of(2019, 3, 1));

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CloseBudgetCommand.class));
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateBudgetCommand.class));
    }

    @Test
    void determineExpense() {
        final Budget budget = Budget.builder()
                .id(1L)
                .start(LocalDate.of(2019, 1, 1))
                .expectedIncome(2300)
                .build();
        budget.new Expense(1L, "Grocery", 300);
        budget.new Expense(2L, "Mortgage", 750);

        final Budget.Expense expense = budget.determineExpense("grocery");

        assertThat(expense).isNotNull();
        assertThat(expense.getName()).isEqualTo("Grocery");
    }

}
