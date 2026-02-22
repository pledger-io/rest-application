package com.jongsoft.finance.budget.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.budget.adapter.api.ExpenseProvider;
import com.jongsoft.finance.core.domain.FilterProvider;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Expenses")
class ExpenseProviderJpaIT extends JpaTestSetup {

    @Inject
    private ExpenseProvider expenseProvider;

    @Inject
    private FilterProvider<ExpenseProvider.FilterCommand> filterFactory;

    @BeforeEach
    void setUp() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/user/budget-provider.sql");
    }

    @Test
    @DisplayName("Lookup all expenses")
    void lookup_byId() {
        var response = expenseProvider.lookup(1L);

        Assertions.assertThat(response.isPresent()).isTrue();
        Assertions.assertThat(response.get().id()).isEqualTo(1L);
        Assertions.assertThat(response.get().name()).isEqualTo("Groceries");
    }

    @Test
    @DisplayName("Lookup expense by id - incorrect user")
    void lookup_byIdWrongUser() {
        var response = expenseProvider.lookup(2L);

        Assertions.assertThat(response.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Lookup expenses by filter")
    void lookup_byFilter() {
        var command = filterFactory.create().name("gro", false);

        var response = expenseProvider.lookup(command);

        Assertions.assertThat(response.total()).isEqualTo(1);
        Assertions.assertThat(response.content().get().id()).isEqualTo(1L);
    }
}
