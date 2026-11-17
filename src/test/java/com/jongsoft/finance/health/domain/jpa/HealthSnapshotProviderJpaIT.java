package com.jongsoft.finance.health.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.health.domain.commands.HealthSnapshotCreated;
import com.jongsoft.finance.health.domain.model.HealthSnapshot;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.Currency;

@DisplayName("Database - Financial health")
class HealthSnapshotProviderJpaIT extends JpaTestSetup {

    @Inject
    private HealthSnapshotProviderJpa provider;

    @BeforeEach
    void setup() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql");
    }

    @Test
    void createNewSnapshot() {
        HealthSnapshotCreated.healthSnapshotCreated(
                YearMonth.of(2010, 1), 6, 1, Currency.getInstance("EUR"));

        HealthSnapshot snapshot = provider.lookup(YearMonth.of(2010, 1))
                .getOrThrow(() -> new RuntimeException("A health snapshot should exist."));
    }
}
