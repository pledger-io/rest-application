package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.jpa.projection.DailySummaryImpl;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.lang.Collections;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

@DisplayName("Database - Transactions")
class TransactionProviderJpaIT extends JpaTestSetup {

    @Inject
    private TransactionProvider transactionProvider;

    @Inject
    private FilterProvider<TransactionProvider.FilterCommand> filterFactory;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/transaction-provider.sql");
    }

    @Test
    void first() {
        var check =
                transactionProvider.first(filterFactory.create().ownAccounts()).get();

        Assertions.assertThat(check.getId()).isEqualTo(1L);
    }

    @Test
    void lookup() {
        var filter = filterFactory
                .create()
                .accounts(Collections.List(new EntityRef(2L)))
                .description("tran", false)
                .onlyIncome(false);

        var check = transactionProvider.lookup(filter);
        Assertions.assertThat(check.content()).hasSize(1);
    }

    @Test
    void lookup_category() {
        var filter = filterFactory
                .create()
                .categories(Collections.List(new EntityRef(1L)))
                .onlyIncome(false);

        var check = transactionProvider.lookup(filter);
        Assertions.assertThat(check.content()).hasSize(1);

        filter = filterFactory
                .create()
                .categories(Collections.List(new EntityRef(2L)))
                .onlyIncome(false);

        check = transactionProvider.lookup(filter);
        Assertions.assertThat(check.content()).hasSize(0);
    }

    @Test
    @DisplayName("Search by currency")
    void lookup_currency() {
        var filter = filterFactory.create().currency("EUR");

        var check = transactionProvider.lookup(filter);
        Assertions.assertThat(check.content()).hasSize(3);
    }

    @Test
    void daily() {
        var check = transactionProvider.daily(filterFactory.create().ownAccounts());
        Assertions.assertThat(check).hasSize(2);
        Assertions.assertThat(check)
                .containsOnly(
                        new DailySummaryImpl(LocalDate.of(2019, 1, 1), BigDecimal.valueOf(30.2D)),
                        new DailySummaryImpl(LocalDate.of(2019, 1, 2), BigDecimal.valueOf(20.2D)));
    }

    @Test
    void monthly() {
        var check = transactionProvider.monthly(filterFactory.create().ownAccounts());
        Assertions.assertThat(check).hasSize(1);
        Assertions.assertThat(check)
                .containsOnly(
                        new DailySummaryImpl(LocalDate.of(2019, 1, 1), BigDecimal.valueOf(50.4D)));
    }

    @Test
    void balance() {
        var check = transactionProvider.balance(filterFactory.create().ownAccounts());

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get()).isEqualByComparingTo(BigDecimal.valueOf(50.4));
    }

    @Test
    void similar() {
        var check = transactionProvider.similar(
                new EntityRef(1L), new EntityRef(2L), 20.2, LocalDate.of(2019, 1, 1));
        Assertions.assertThat(check).hasSize(1);
    }
}
