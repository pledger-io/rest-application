package com.jongsoft.finance.health.domain.jpa;

import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.health.adapter.api.HealthSnapshotProvider;
import com.jongsoft.finance.health.domain.commands.HealthComponentComputed;
import com.jongsoft.finance.health.domain.commands.HealthSnapshotCompleted;
import com.jongsoft.finance.health.domain.commands.HealthSnapshotCreated;
import com.jongsoft.finance.health.domain.commands.InvalidateSnapshot;
import com.jongsoft.finance.health.domain.jpa.entity.HealthSnapshotJpa;
import com.jongsoft.finance.health.domain.model.HealthSnapshot;
import com.jongsoft.lang.control.Optional;
import com.jongsoft.lang.time.Range;

import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
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

    @EventListener
    void snapshotCompleted(HealthSnapshotCompleted command) {
        logger.info("[{}] Marking snapshot as completed.", command.yearMonth());

        entityManager
                .update(HealthSnapshotJpa.class)
                .set("status", command.status())
                .set("rating", command.healthRating())
                .set("score", command.score())
                .set("computedAt", LocalDateTime.now())
                .fieldEq("user", entityManager.currentUser())
                .fieldEq("yearMonth", command.yearMonth().toString())
                .execute();
    }

    @EventListener
    void componentComputed(HealthComponentComputed command) {
        logger.info("[{}] Component computed {}.", command.yearMonth(), command.type());
        var updatePipeline = entityManager
                .update(HealthSnapshotJpa.class)
                .fieldEq("user", entityManager.currentUser())
                .fieldEq("yearMonth", command.yearMonth().toString());

        switch (command.type()) {
            case DEBT -> {
                updatePipeline
                        .set("dtiScore", command.score())
                        .set("debtToIncome", command.value())
                        .set("income", command.details().get("income"))
                        .set("debtPayments", command.details().get("debtPayments"));
            }
            case BUDGET -> {
                updatePipeline
                        .set("budgetAdherence", command.value())
                        .set("budgetScore", command.score());
            }
            case SAVINGS ->
                updatePipeline
                        .set("savingsRate", command.value())
                        .set("savingsScore", command.score())
                        .set("income", command.details().get("income"))
                        .set("expenses", command.details().get("expenses"));
        }

        updatePipeline.execute();
    }

    @EventListener
    void invalidateSnapshot(InvalidateSnapshot command) {
        logger.info("[{}] Invalidating snapshot.", command.yearMonth());
        entityManager
                .getEntityManager()
                .createQuery(
                        "delete from HealthSnapshotJpa where user = :user and yearMonth = :yearMonth")
                .setParameter("user", entityManager.currentUser())
                .setParameter("yearMonth", command.yearMonth().toString())
                .executeUpdate();
    }
}
