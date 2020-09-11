package com.jongsoft.finance.jpa.transaction;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.transaction.TransactionScheduleProvider;
import com.jongsoft.finance.jpa.JpaTestSetup;

import io.micronaut.test.annotation.MockBean;

class TransactionScheduleProviderJpaTest extends JpaTestSetup {

    @Inject
    private TransactionScheduleProvider scheduleProvider;

    @Inject
    private AuthenticationFacade authenticationFacade;

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
        var check = scheduleProvider.lookup();

        Assertions.assertThat(check).hasSize(1);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

}
