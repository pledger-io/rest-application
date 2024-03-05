package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExpenseProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ExpenseProvider expenseProvider;

    @Inject
    private FilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/user/budget-provider.sql"
        );
    }

    @Test
    void lookup_byId() {
        var response = expenseProvider.lookup(1L);

        Assertions.assertThat(response.isPresent()).isTrue();
        Assertions.assertThat(response.get().id()).isEqualTo(1L);
        Assertions.assertThat(response.get().name()).isEqualTo("Groceries");
    }

    @Test
    void lookup_byIdWrongUser() {
        var response = expenseProvider.lookup(2L);

        Assertions.assertThat(response.isPresent()).isFalse();
    }

    @Test
    void lookup_byFilter() {
        var command = filterFactory.expense()
                .name("gro", false);

        var response = expenseProvider.lookup(command);

        Assertions.assertThat(response.total()).isEqualTo(1);
        Assertions.assertThat(response.content().get().id()).isEqualTo(1L);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
