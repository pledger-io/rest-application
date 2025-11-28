package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionScheduleProviderJpaIT extends JpaTestSetup {

    @Inject
    private TransactionScheduleProvider transactionScheduleProvider;

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private FilterFactory filterFactory;

    @BeforeEach
    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/account/account-provider.sql",
                "sql/transaction/schedule-provider.sql"
        );
    }

    @Test
    @DisplayName("List all scheduled transactions")
    void lookup() {
        var check = transactionScheduleProvider.lookup();
        Assertions.assertThat(check).hasSize(1);
    }

    @Test
    void lookup_filter() {
        var check = transactionScheduleProvider.lookup(filterFactory.schedule().activeOnly());

        Assertions.assertThat(check.pages()).isEqualTo(1);
    }

    @MockBean
    @Replaces
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

}
