package com.jongsoft.finance.jpa.user;

import java.time.LocalDate;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.events.BudgetClosedEvent;
import com.jongsoft.finance.domain.user.events.BudgetCreatedEvent;
import com.jongsoft.finance.domain.user.events.BudgetExpenseCreatedEvent;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.user.entity.BudgetJpa;
import com.jongsoft.lang.API;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;

class BudgetEventListenerTest extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    void init() {
        loadDataset(
                "sql/base-setup.sql",
                "sql/user/budget-provider.sql"
        );
    }

    @Test
    void handleBudgetCreatedEvent() {
        init();
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user-not");

        eventPublisher.publishEvent(new BudgetCreatedEvent(
                this,
                Budget.builder()
                        .expectedIncome(2500)
                        .start(LocalDate.of(2018, 1, 1))
                        .expenses(API.List(Budget.Expense.builder()
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
        init();
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");

        eventPublisher.publishEvent(new BudgetClosedEvent(
                this,
                2L,
                LocalDate.of(2020, 1, 1)));

        var check = entityManager.find(BudgetJpa.class, 2L);
        Assertions.assertThat(check.getUntil()).isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    void handleExpenseCreatedEvent() {
        init();
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");

        eventPublisher.publishEvent(new BudgetExpenseCreatedEvent(
                this,
                "Created expense",
                LocalDate.of(2019, 2, 1),
                400,
                500));

        var check = entityManager.find(BudgetJpa.class, 2L);
        Assertions.assertThat(check.getExpenses()).hasSize(3);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
