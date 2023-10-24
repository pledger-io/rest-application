package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

class BudgetProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private BudgetProvider budgetProvider;

    @BeforeEach
    void setup() throws IOException {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/user/budget-provider.sql"
        );
    }

    @Test
    void lookup() throws IOException {
        var check = budgetProvider.lookup();
        Assertions.assertThat(check).hasSize(2);
    }

    @Test
    void lookup_201901() throws IOException {
        var check = budgetProvider.lookup(2019, 1).get();

        Assertions.assertThat(check.getExpenses()).hasSize(2);
        Assertions.assertThat(check.getExpectedIncome()).isEqualTo(2500);
    }

    @Test
    void lookup_202001() throws IOException {
        var check = budgetProvider.lookup(2020, 1).get();

        Assertions.assertThat(check.getExpenses()).hasSize(2);
        Assertions.assertThat(check.getExpectedIncome()).isEqualTo(2800);
    }

    @Test
    void first() throws IOException {
        var check = budgetProvider.first().get();

        Assertions.assertThat(check.getExpenses()).hasSize(2);
        Assertions.assertThat(check.getExpectedIncome()).isEqualTo(2500);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
