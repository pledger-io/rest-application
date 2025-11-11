package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.messaging.commands.budget.UpdateExpenseCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;

class UpdateExpenseHandlerTest extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;
    @Inject
    private EntityManager entityManager;
    @Inject
    private ApplicationEventPublisher<UpdateExpenseCommand> eventPublisher;

    @BeforeEach
    void setUp() {
        Mockito.when(authenticationFacade.authenticated())
                .thenReturn("demo-user");
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/user/budget-provider.sql"
        );
    }

    @Test
    void updateExpense() {
        eventPublisher.publishEvent(new UpdateExpenseCommand(1L, BigDecimal.valueOf(150)));

        var updated = entityManager.createQuery("select e from ExpensePeriodJpa e where e.id = 3", ExpensePeriodJpa.class)
                .getSingleResult();

        Assertions.assertThat(updated).isNotNull();
        Assertions.assertThat(updated.getLowerBound()).isEqualTo(BigDecimal.valueOf(149.99));
        Assertions.assertThat(updated.getUpperBound().setScale(0, RoundingMode.FLOOR)).isEqualTo(BigDecimal.valueOf(150));
    }

    @MockBean
    @Replaces
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
