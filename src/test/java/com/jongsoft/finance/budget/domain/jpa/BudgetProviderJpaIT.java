package com.jongsoft.finance.budget.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("Database - Budgets")
class BudgetProviderJpaIT extends JpaTestSetup {

    @Inject
    private BudgetProvider budgetProvider;

    @BeforeEach
    void setup() throws IOException {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/user/budget-provider.sql");
    }

    @Test
    @DisplayName("Lookup all budgets")
    void lookup() throws IOException {
        var check = budgetProvider.lookup();
        Assertions.assertThat(check).hasSize(2);
    }

    @Test
    @DisplayName("Lookup budget for year and month")
    void lookup_201901() throws IOException {
        var check = budgetProvider.lookup(2019, 1).get();

        Assertions.assertThat(check.getExpenses()).hasSize(2);
        Assertions.assertThat(check.getExpectedIncome()).isEqualTo(2500);
    }

    @Test
    @DisplayName("Lookup budget for year and month - incorrect user")
    void lookup_202001() throws IOException {
        var check = budgetProvider.lookup(2020, 1).get();

        Assertions.assertThat(check.getExpenses()).hasSize(2);
        Assertions.assertThat(check.getExpectedIncome()).isEqualTo(2800);
    }

    @Test
    @DisplayName("First budget")
    void first() throws IOException {
        var check = budgetProvider.first().get();

        Assertions.assertThat(check.getExpenses()).hasSize(2);
        Assertions.assertThat(check.getExpectedIncome()).isEqualTo(2500);
    }
}
