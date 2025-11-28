package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

class TransactionProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private TransactionProvider transactionProvider;

    @Inject
    private FilterFactory filterFactory;

    @BeforeEach
    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/transaction-provider.sql"
        );
    }

    @Test
    void first() {
        var check = transactionProvider.first(filterFactory.transaction().ownAccounts())
                .get();

        Assertions.assertThat(check.getId())
                .isEqualTo(1L);
    }

    @Test
    void lookup() {
        var filter = filterFactory.transaction()
                .accounts(Collections.List(new EntityRef(2L)))
                .description("tran", false)
                .onlyIncome(false);

        var check = transactionProvider.lookup(filter);
        Assertions.assertThat(check.content()).hasSize(1);
    }

    @Test
    @DisplayName("Search by currency")
    void lookup_currency() {
        var filter = filterFactory.transaction()
                .currency("EUR");

        var check = transactionProvider.lookup(filter);
        Assertions.assertThat(check.content()).hasSize(3);
    }

    @Test
    void daily() {
        var check = transactionProvider.daily(filterFactory.transaction().ownAccounts());
        Assertions.assertThat(check).hasSize(2);
        Assertions.assertThat(check).containsOnly(
                new DailySummaryImpl(LocalDate.of(2019, 1, 1), BigDecimal.valueOf(30.2D)),
                new DailySummaryImpl(LocalDate.of(2019, 1, 2), BigDecimal.valueOf(20.2D)));
    }

    @Test
    void monthly() {
        var check = transactionProvider.monthly(filterFactory.transaction().ownAccounts());
        Assertions.assertThat(check).hasSize(1);
        Assertions.assertThat(check).containsOnly(
                new DailySummaryImpl(LocalDate.of(2019, 1, 1), BigDecimal.valueOf(50.4D)));
    }

    @Test
    void balance() {
        var check = transactionProvider.balance(filterFactory.transaction().ownAccounts());

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get()).isEqualByComparingTo(BigDecimal.valueOf(50.4));
    }

    @Test
    void similar() {
        var check = transactionProvider.similar(new EntityRef(1L), new EntityRef(2L), 20.2, LocalDate.of(2019, 1, 1));
        Assertions.assertThat(check).hasSize(1);
    }

    @MockBean
    @Replaces
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

}
