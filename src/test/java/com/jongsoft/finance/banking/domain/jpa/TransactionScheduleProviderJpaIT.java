package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.core.domain.FilterProvider;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Transaction Schedule")
class TransactionScheduleProviderJpaIT extends JpaTestSetup {

    @Inject
    private TransactionScheduleProvider transactionScheduleProvider;

    @Inject
    private FilterProvider<TransactionScheduleProvider.FilterCommand> filterFactory;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/account/account-provider.sql",
                "sql/transaction/schedule-provider.sql");
    }

    @Test
    @DisplayName("List all scheduled transactions")
    void lookup() {
        var check = transactionScheduleProvider.lookup();
        Assertions.assertThat(check).hasSize(1);
    }

    @Test
    void lookup_filter() {
        var check = transactionScheduleProvider.lookup(filterFactory.create().activeOnly());

        Assertions.assertThat(check.pages()).isEqualTo(1);
    }
}
