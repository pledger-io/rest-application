package com.jongsoft.finance.budget.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.budget.domain.commands.UpdateExpenseCommand;
import com.jongsoft.finance.budget.domain.jpa.entity.ExpensePeriodJpa;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

@DisplayName("Database - Expense mutations")
class UpdateExpenseHandlerTest extends JpaTestSetup {

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/user/budget-provider.sql");
    }

    @Test
    void updateExpense() {
        UpdateExpenseCommand.expenseUpdated(1L, BigDecimal.valueOf(150));

        var updated = entityManager
                .createQuery(
                        "select e from ExpensePeriodJpa e where e.id = 3", ExpensePeriodJpa.class)
                .getSingleResult();

        Assertions.assertThat(updated).isNotNull();
        Assertions.assertThat(updated.getLowerBound()).isEqualTo(BigDecimal.valueOf(149.99));
        Assertions.assertThat(updated.getUpperBound().setScale(0, RoundingMode.FLOOR))
                .isEqualTo(BigDecimal.valueOf(150));
    }
}
