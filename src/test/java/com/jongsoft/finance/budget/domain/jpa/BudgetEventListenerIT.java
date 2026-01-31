package com.jongsoft.finance.budget.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.budget.domain.commands.CloseBudgetCommand;
import com.jongsoft.finance.budget.domain.commands.CreateBudgetCommand;
import com.jongsoft.finance.budget.domain.commands.CreateExpenseCommand;
import com.jongsoft.finance.budget.domain.jpa.entity.BudgetJpa;
import com.jongsoft.finance.core.domain.AuthenticationFacade;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@DisplayName("Database - Budget mutations")
class BudgetEventListenerIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void init() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/user/budget-provider.sql");
    }

    @Test
    @DisplayName("Create new budget")
    void handleBudgetCreatedEvent() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user-not");
        CreateBudgetCommand.budgetCreated(
                2500,
                LocalDate.of(2018, 1, 1),
                List.of(new CreateBudgetCommand.CreateExpense(2, 150)));

        var query = entityManager.createQuery(
                "select b from BudgetJpa b where b.user.username = 'demo-user-not'");
        var check = (BudgetJpa) query.getSingleResult();

        Assertions.assertThat(check.getExpectedIncome()).isEqualTo(2500);
        Assertions.assertThat(check.getExpenses()).hasSize(1);
        Assertions.assertThat(check.getExpenses().iterator().next().getExpense().getName())
                .isEqualTo("Groceries");
    }

    @Test
    @DisplayName("Close budget")
    void handleBudgetClosedEvent() {
        CloseBudgetCommand.budgetClosed(2L, LocalDate.of(2020, 1, 1));

        var check = entityManager.find(BudgetJpa.class, 2L);
        Assertions.assertThat(check.getUntil()).isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    @DisplayName("Create new expense")
    void handleExpenseCreatedEvent() {
        CreateExpenseCommand.expenseCreated(
                "Created expense", LocalDate.of(2019, 2, 1), BigDecimal.valueOf(500));

        var check = entityManager.find(BudgetJpa.class, 2L);
        Assertions.assertThat(check.getExpenses()).hasSize(3);
    }
}
