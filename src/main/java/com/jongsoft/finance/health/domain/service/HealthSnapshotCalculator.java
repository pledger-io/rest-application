package com.jongsoft.finance.health.domain.service;

import com.jongsoft.finance.health.adapter.api.HealthSnapshotProvider;
import com.jongsoft.finance.health.domain.jpa.HealthSnapshotJpaProvider;
import com.jongsoft.finance.health.domain.model.HealthSnapshot;

import jakarta.inject.Singleton;

import java.time.YearMonth;

@Singleton
class HealthSnapshotCalculator implements HealthSnapshotProvider {

    private final HealthSnapshotJpaProvider healthSnapshotJpaProvider;

    HealthSnapshotCalculator(HealthSnapshotJpaProvider healthSnapshotJpaProvider) {
        this.healthSnapshotJpaProvider = healthSnapshotJpaProvider;
    }

    @Override
    public HealthSnapshot lookup(YearMonth yearMonth) {
        return healthSnapshotJpaProvider
                .get(yearMonth.getYear(), yearMonth.getMonthValue())
                .getOrSupply(() -> calculateMissing(yearMonth));
    }

    private HealthSnapshot calculateMissing(YearMonth yearMonth) {
        return null;
    }
}
