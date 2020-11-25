package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.jpa.FilterFactoryJpa;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.transaction.entity.DailySummaryImpl;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.test.annotation.MockBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.time.LocalDate;

class TransactionProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private TransactionProvider transactionProvider;

    private FilterFactory filterFactory = new FilterFactoryJpa();

    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/base-setup.sql",
                "sql/transaction/transaction-provider.sql"
        );
    }

    @Test
    void first() {
        setup();
        var check = transactionProvider.first(filterFactory.transaction().ownAccounts())
                .blockingGet();

        Assertions.assertThat(check.getId()).isEqualTo(1L);
    }

    @Test
    void lookup() {
        setup();
        var filter = filterFactory.transaction()
                .accounts(Collections.List(new EntityRef(2L)))
                .description("tran", false)
                .onlyIncome(false);

        var check = transactionProvider.lookup(filter);
        Assertions.assertThat(check.content()).hasSize(1);
    }

    @Test
    void lookup_currency() {
        setup();
        var filter = filterFactory.transaction()
                .currency("EUR");

        var check = transactionProvider.lookup(filter);
        Assertions.assertThat(check.content()).hasSize(2);
    }

    @Test
    void daily() {
        setup();
        var check = transactionProvider.daily(filterFactory.transaction().ownAccounts());
        Assertions.assertThat(check).hasSize(2);
        Assertions.assertThat(check).containsOnly(
                new DailySummaryImpl(LocalDate.of(2019, 1, 1), 20.2D),
                new DailySummaryImpl(LocalDate.of(2019, 1, 2), 20.2D));
    }

    @Test
    void balance() {
        setup();
        var check = transactionProvider.balance(filterFactory.transaction().ownAccounts());

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get()).isEqualTo(40.4);
    }

    @Test
    void similar() {
        setup();
        var check = transactionProvider.similar(new EntityRef(1L), new EntityRef(2L), 20.2, LocalDate.of(2019, 1, 1));
        Assertions.assertThat(check).hasSize(1);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

}
