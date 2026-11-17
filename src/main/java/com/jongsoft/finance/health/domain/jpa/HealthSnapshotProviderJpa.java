package com.jongsoft.finance.health.domain.jpa;

import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.health.adapter.api.HealthSnapshotProvider;
import com.jongsoft.finance.health.domain.commands.HealthSnapshotCreated;
import com.jongsoft.finance.health.domain.jpa.entity.HealthSnapshotJpa;
import com.jongsoft.finance.health.domain.model.HealthSnapshot;
import com.jongsoft.lang.control.Optional;

import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;

@Singleton
@Transactional
class HealthSnapshotProviderJpa implements HealthSnapshotProvider {
    private final Logger logger;
    private final ReactiveEntityManager entityManager;
    private final HealthSnapshotMapper mapper;

    public HealthSnapshotProviderJpa(
            ReactiveEntityManager entityManager, HealthSnapshotMapper mapper) {
        this.entityManager = entityManager;
        this.mapper = mapper;
        logger = LoggerFactory.getLogger(HealthSnapshotProviderJpa.class);
    }

    @Override
    public Optional<HealthSnapshot> lookup(YearMonth yearMonth) {
        logger.debug("[{}] - looking up snapshot for user.", yearMonth);

        return entityManager
                .from(HealthSnapshotJpa.class)
                .fieldEq("yearMonth", yearMonth.toString())
                .fieldEq("user", entityManager.currentUser())
                .singleResult()
                .map(mapper::convertToDomain);
    }

    @EventListener
    void snapshotCreated(HealthSnapshotCreated healthSnapshotCreated) {
        logger.info("[{}] Creating new health snapshot.", healthSnapshotCreated.yearMonth());

        HealthSnapshotJpa entity = HealthSnapshotJpa.of(
                entityManager.currentUser(),
                healthSnapshotCreated.yearMonth(),
                healthSnapshotCreated.windowMonths(),
                healthSnapshotCreated.algorithmVersion(),
                healthSnapshotCreated.currency());

        entityManager.persist(entity);
    }
}
