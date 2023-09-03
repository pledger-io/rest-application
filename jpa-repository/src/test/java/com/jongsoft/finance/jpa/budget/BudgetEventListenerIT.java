package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.messaging.commands.budget.CloseBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateExpenseCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;

class BudgetEventListenerIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void init() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/user/budget-provider.sql"
        );
    }

    @Test
    void handleBudgetCreatedEvent() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user-not");

        eventPublisher.publishEvent(new CreateBudgetCommand(
                Budget.builder()
                        .expectedIncome(2500)
                        .start(LocalDate.of(2018, 1, 1))
                        .expenses(Collections.List(Budget.Expense.builder()
                                .id(2L)
                                .name("Groceries")
                                .upperBound(200)
                                .lowerBound(100)
                                .build()))
                        .build()));

        var query = entityManager.createQuery("select b from BudgetJpa b where b.user.username = 'demo-user-not'");
        var check = (BudgetJpa) query.getSingleResult();

        Assertions.assertThat(check.getExpectedIncome()).isEqualTo(2500);
        Assertions.assertThat(check.getExpenses()).hasSize(1);
        Assertions.assertThat(check.getExpenses().iterator().next().getExpense().getName()).isEqualTo("Groceries");
    }

    @Test
    void handleBudgetClosedEvent() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");

        eventPublisher.publishEvent(new CloseBudgetCommand(2L, LocalDate.of(2020, 1, 1)));

        var check = entityManager.find(BudgetJpa.class, 2L);
        Assertions.assertThat(check.getUntil()).isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    void handleExpenseCreatedEvent() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");

        eventPublisher.publishEvent(new CreateExpenseCommand(
                "Created expense",
                LocalDate.of(2019, 2, 1),
                BigDecimal.valueOf(500)));

        var check = entityManager.find(BudgetJpa.class, 2L);
        Assertions.assertThat(check.getExpenses()).hasSize(3);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
