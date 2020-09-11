package com.jongsoft.finance.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.domain.user.events.BudgetClosedEvent;
import com.jongsoft.finance.domain.user.events.BudgetCreatedEvent;
import com.jongsoft.finance.domain.user.events.BudgetExpenseCreatedEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.lang.API;

import io.micronaut.context.event.ApplicationEventPublisher;

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
                .expenses(API.List())
                .build();

        budget.createExpense("Grocery", 200, 400);

        assertThat(budget.getExpenses()).hasSize(1);
        assertThat(budget.getExpenses().get(0).getName()).isEqualTo("Grocery");
        assertThat(budget.getExpenses().get(0).getLowerBound()).isEqualTo(200D);
        assertThat(budget.getExpenses().get(0).getUpperBound()).isEqualTo(400D);

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(BudgetExpenseCreatedEvent.class));
    }

    @Test
    void createExpense_exceedsLimit() {
        final Budget budget = Budget.builder()
                .id(1L)
                .start(LocalDate.of(2019, 1, 1))
                .expectedIncome(50)
                .expenses(API.List())
                .build();

        final IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> budget.createExpense("Grocery", 200, 400));

        assertThat(thrown.getMessage()).isEqualTo("Expected expenses exceeds the expected income.");
    }

    @Test
    void createExpense_alreadyClosed() {
        final Budget budget = Budget.builder()
                .id(1L)
                .start(LocalDate.of(2019, 1, 1))
                .end(LocalDate.of(2019, 1, 31))
                .expectedIncome(50)
                .expenses(API.List())
                .build();

        final IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> budget.createExpense("Grocery", 200, 400));

        assertThat(thrown.getMessage()).isEqualTo("Cannot add expense to an already closed budget period.");
    }

    @Test
    void indexBudget() {
        final Budget budget = Budget.builder()
                .id(1L)
                .start(LocalDate.of(2019, 1, 1))
                .expectedIncome(2300)
                .expenses(API.List(
                        Budget.Expense.builder()
                                .id(1L)
                                .lowerBound(250)
                                .upperBound(350)
                                .name("Grocery")
                                .build(),
                        Budget.Expense.builder()
                                .id(1L)
                                .lowerBound(650)
                                .upperBound(850)
                                .name("Mortgage")
                                .build()))
                .build();

        Budget indexedBudget = budget.indexBudget(LocalDate.of(2019, 3, 1), 2550);

        assertThat(indexedBudget.getExpectedIncome()).isEqualTo(2550D);
        assertThat(indexedBudget.getExpenses()).hasSize(2);

        assertThat(indexedBudget.getExpenses().get(0).getName()).isEqualTo("Grocery");
        assertThat(indexedBudget.getExpenses().get(0).getLowerBound()).isEqualTo(278D);
        assertThat(indexedBudget.getExpenses().get(0).getUpperBound()).isEqualTo(389D);
        assertThat(indexedBudget.getExpenses().get(1).getName()).isEqualTo("Mortgage");
        assertThat(indexedBudget.getExpenses().get(1).getLowerBound()).isEqualTo(721D);
        assertThat(indexedBudget.getExpenses().get(1).getUpperBound()).isEqualTo(943D);
        assertThat(budget.getEnd()).isEqualTo(LocalDate.of(2019, 3, 1));

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(BudgetClosedEvent.class));
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(BudgetCreatedEvent.class));
    }

    @Test
    void determineExpense() {
        final Budget budget = Budget.builder()
                .id(1L)
                .start(LocalDate.of(2019, 1, 1))
                .expectedIncome(2300)
                .expenses(API.List(
                        Budget.Expense.builder()
                                .id(1L)
                                .lowerBound(250)
                                .upperBound(350)
                                .name("Grocery")
                                .build(),
                        Budget.Expense.builder()
                                .id(1L)
                                .lowerBound(650)
                                .upperBound(850)
                                .name("Mortgage")
                                .build()))
                .build();

        final Budget.Expense expense = budget.determineExpense("grocery");

        assertThat(expense).isNotNull();
        assertThat(expense.getName()).isEqualTo("Grocery");
    }

}
