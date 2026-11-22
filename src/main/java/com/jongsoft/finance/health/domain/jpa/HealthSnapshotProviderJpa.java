package com.jongsoft.finance.health.domain.jpa;

import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.health.adapter.api.HealthSnapshotProvider;
import com.jongsoft.finance.health.domain.commands.HealthSnapshotCreated;
import com.jongsoft.finance.health.domain.jpa.entity.HealthSnapshotJpa;
import com.jongsoft.finance.health.domain.model.HealthSnapshot;
import com.jongsoft.lang.control.Optional;
import com.jongsoft.lang.time.Range;

import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<HealthSnapshot> lookup(Range<YearMonth> range) {
        YearMonth start = range.from();
        List<String> relevantMonths = new ArrayList<>();
        while (start.isBefore(range.until())) {
            relevantMonths.add(start.toString());
            start = start.plusMonths(1);
        }

        return entityManager
                .from(HealthSnapshotJpa.class)
                .fieldEq("user", entityManager.currentUser())
                .fieldEqOneOf("yearMonth", relevantMonths.toArray())
                .stream()
                .map(mapper::convertToDomain)
                .toList();
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
