package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.savings.SavingGoalJpa;
import com.jongsoft.finance.messaging.commands.savings.CreateSavingGoalCommand;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingGoalEventTest extends JpaTestSetup {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    void setup() {
        loadDataset(
                "sql/base-setup.sql",
                "sql/account/account-provider.sql"
        );
    }

    @Test
    void createSavingGoal() {
        setup();

        eventPublisher.publishEvent(new CreateSavingGoalCommand(
                1L,
                "New savings",
                BigDecimal.TEN,
                LocalDate.now().plusDays(10)));

        var check = entityManager.createQuery("from SavingGoalJpa where name = :name", SavingGoalJpa.class)
                .setParameter("name", "New savings")
                .getSingleResult();

        Assertions.assertThat(check.getAccount().getIban()).isEqualTo("NLJND200001928233");
        Assertions.assertThat(check.getGoal()).isEqualByComparingTo("10");
        Assertions.assertThat(check.getTargetDate()).isEqualTo(LocalDate.now().plusDays(10));
    }

}
