package com.jongsoft.finance.spending.domain.jpa;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.spending.adapter.api.AnalyzeJobProvider;
import com.jongsoft.finance.spending.domain.commands.CompleteAnalyzeJob;
import com.jongsoft.finance.spending.domain.commands.CreateAnalyzeJob;
import com.jongsoft.finance.spending.domain.commands.FailAnalyzeJob;
import com.jongsoft.finance.spending.domain.jpa.entity.AnalyzeJobJpa;
import com.jongsoft.finance.spending.domain.jpa.mapper.AnalyzeJobMapper;
import com.jongsoft.finance.spending.domain.model.AnalyzeJob;

import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

@Singleton
class AnalyzeJobProviderJpa implements AnalyzeJobProvider {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(AnalyzeJobProviderJpa.class);

    private final ReactiveEntityManager entityManager;
    private final AnalyzeJobMapper analyzeJobMapper;

    public AnalyzeJobProviderJpa(
            ReactiveEntityManager entityManager, AnalyzeJobMapper analyzeJobMapper) {
        this.entityManager = entityManager;
        this.analyzeJobMapper = analyzeJobMapper;
    }

    @Override
    public Optional<AnalyzeJob> first() {
        return entityManager
                .from(AnalyzeJobJpa.class)
                .fieldEq("completed", false)
                .fieldEq("failed", false)
                .orderBy("yearMonth", true)
                .limit(1)
                .singleResult()
                .map(analyzeJobMapper::toDomain)
                .map(Optional::ofNullable)
                .getOrSupply(Optional::empty);
    }

    @EventListener
    public void createAnalyzeJob(CreateAnalyzeJob command) {
        log.info("Creating analyze job for month {}.", command.month());

        var entity = new AnalyzeJobJpa();
        entity.setId(UUID.randomUUID().toString());
        entity.setUser(entityManager
                .from(UserAccountJpa.class)
                .fieldEq("username", command.user().email())
                .singleResult()
                .get());
        entity.setYearMonth(command.month().toString());

        entityManager.getEntityManager().persist(entity);
    }

    @EventListener
    public void completeAnalyzeJob(CompleteAnalyzeJob command) {
        log.info("Completing analyze job for month {}.", command.month());

        entityManager
                .update(AnalyzeJobJpa.class)
                .fieldEq("yearMonth", command.month().toString())
                .fieldEq("completed", false)
                .fieldEq("user.username", command.user().email())
                .set("completed", true)
                .execute();
    }

    @EventListener
    public void completeAnalyzeJob(FailAnalyzeJob command) {
        log.info("Failing analyze job for month {}.", command.month());

        entityManager
                .update(AnalyzeJobJpa.class)
                .fieldEq("yearMonth", command.month().toString())
                .fieldEq("completed", false)
                .fieldEq("user.username", command.user().email())
                .set("failed", true)
                .execute();
    }
}
