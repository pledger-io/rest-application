package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.jpa.FilterFactoryJpa;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;

@Disabled
class TransactionScheduleProviderJpaIT extends JpaTestSetup {

    @Inject
    private TransactionScheduleProvider transactionScheduleProvider;

    @Inject
    private AuthenticationFacade authenticationFacade;

    private FilterFactory filterFactory = new FilterFactoryJpa();

    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/base-setup.sql",
                "sql/account/account-provider.sql",
                "sql/transaction/schedule-provider.sql"
        );
    }

    @Test
    void lookup() {
        setup();
        var check = transactionScheduleProvider.lookup();

        Assertions.assertThat(check).hasSize(1);
    }

    @Test
    void lookup_filter() {
        setup();

        var check = transactionScheduleProvider.lookup(filterFactory.schedule().activeOnly());

        Assertions.assertThat(check.pages()).isEqualTo(1);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

}
