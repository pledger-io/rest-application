package com.jongsoft.finance.jpa.insight;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.insight.AnalyzeJob;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.commands.insight.CompleteAnalyzeJob;
import com.jongsoft.finance.messaging.commands.insight.CreateAnalyzeJob;
import com.jongsoft.finance.messaging.commands.insight.FailAnalyzeJob;
import com.jongsoft.finance.providers.AnalyzeJobProvider;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Singleton
class AnalyzeJobProviderJpa implements AnalyzeJobProvider {

    private final ReactiveEntityManager entityManager;

    public AnalyzeJobProviderJpa(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
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
                .map(this::convert)
                .map(Optional::ofNullable)
                .getOrSupply(Optional::empty);
    }

    @BusinessEventListener
    public void createAnalyzeJob(CreateAnalyzeJob command) {
        log.info("Creating analyze job for month {}.", command.month());

        var entity = new AnalyzeJobJpa();
        entity.setId(UUID.randomUUID().toString());
        entity.setUser(
                entityManager
                        .from(UserAccountJpa.class)
                        .fieldEq("username", command.user().email())
                        .singleResult()
                        .get());
        entity.setYearMonth(command.month().toString());

        entityManager.getEntityManager().persist(entity);
    }

    @BusinessEventListener
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

    @BusinessEventListener
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

    private AnalyzeJob convert(AnalyzeJobJpa entity) {
        return AnalyzeJob.builder()
                .jobId(entity.getId())
                .month(YearMonth.parse(entity.getYearMonth()))
                .user(new UserIdentifier(entity.getUser().getUsername()))
                .completed(entity.isCompleted())
                .build();
    }
}
