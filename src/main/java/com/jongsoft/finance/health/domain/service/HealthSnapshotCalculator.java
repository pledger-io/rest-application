package com.jongsoft.finance.health.domain.service;

import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.health.domain.commands.ComputeHealth;
import com.jongsoft.finance.health.domain.model.HealthSnapshot;

import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;

@Singleton
class HealthSnapshotCalculator {
    private final Logger logger;
    private final CurrentUserProvider currentUserProvider;

    HealthSnapshotCalculator(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
        logger = LoggerFactory.getLogger(HealthSnapshotCalculator.class);
    }

    @EventListener
    void computeHealthSnapshot(ComputeHealth command) {
        logger.info("Computing health snapshot for {}.", command.yearMonth());
        UserAccount user = currentUserProvider.currentUser();
        YearMonth yearMonth = command.yearMonth();

        HealthSnapshot.of(yearMonth, 12, 1, user.getPrimaryCurrency());

        // todo add calculation logic
    }
}
