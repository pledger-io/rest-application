package com.jongsoft.finance.spending.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.spending.adapter.api.SpendingInsightProvider;
import com.jongsoft.finance.spending.domain.commands.CleanInsightsForMonth;
import com.jongsoft.finance.spending.domain.commands.CreateSpendingInsight;
import com.jongsoft.finance.spending.domain.jpa.entity.SpendingInsightJpa;
import com.jongsoft.finance.spending.domain.jpa.filter.SpendingInsightFilterCommand;
import com.jongsoft.finance.spending.domain.jpa.mapper.SpendingInsightMapper;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@ReadOnly
@Singleton
public class SpendingInsightProviderJpa implements SpendingInsightProvider {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(SpendingInsightProviderJpa.class);

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final SpendingInsightMapper mapper;

    public SpendingInsightProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            SpendingInsightMapper mapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.mapper = mapper;
    }

    @Override
    public Sequence<SpendingInsight> lookup() {
        log.trace("Fetching all spending insights");

        return entityManager
                .from(SpendingInsightJpa.class)
                .joinFetch("user")
                .fieldEq("user.username", authenticationFacade.authenticated())
                .stream()
                .map(mapper::toModel)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public Optional<SpendingInsight> lookup(String category) {
        log.trace("Fetching spending insight for category: {}", category);

        return entityManager
                .from(SpendingInsightJpa.class)
                .joinFetch("user")
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("category", category)
                .singleResult()
                .map(mapper::toModel);
    }

    @Override
    public Sequence<SpendingInsight> lookup(YearMonth yearMonth) {
        log.trace("Fetching spending insights for year-month: {}", yearMonth);

        return entityManager
                .from(SpendingInsightJpa.class)
                .joinFetch("user")
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("yearMonth", yearMonth.toString())
                .stream()
                .map(mapper::toModel)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public ResultPage<SpendingInsight> lookup(SpendingInsightProvider.FilterCommand filter) {
        log.trace("Fetching spending insights with filter: {}", filter);

        if (filter instanceof SpendingInsightFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());
            return entityManager.from(delegate).paged().map(mapper::toModel);
        }

        throw new IllegalStateException("Cannot use non-JPA filter on SpendingInsightProviderJpa");
    }

    @EventListener
    public void save(CreateSpendingInsight command) {
        log.trace("Saving spending insight: {}", command);

        // Convert metadata to string values
        Map<String, String> metadata = new HashMap<>();
        for (Map.Entry<String, Object> entry : command.metadata().entrySet()) {
            metadata.put(
                    entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
        }

        // Create the JPA entity
        SpendingInsightJpa entity = new SpendingInsightJpa(
                command.type(),
                command.category(),
                command.severity(),
                command.score(),
                command.detectedDate(),
                command.message(),
                YearMonth.from(command.detectedDate()),
                metadata,
                command.transactionId(),
                entityManager.currentUser());

        // Save the entity
        entityManager.persist(entity);
    }

    @EventListener
    public void cleanForMonth(CleanInsightsForMonth command) {
        log.trace("Cleaning spending insights for month: {}", command.month());
        entityManager
                .getEntityManager()
                .createQuery(
                        "DELETE FROM SpendingInsightJpa WHERE yearMonth = :yearMonth and user.id in (select id from UserAccountJpa a where a.username = :username)")
                .setParameter("yearMonth", command.month().toString())
                .setParameter("username", authenticationFacade.authenticated())
                .executeUpdate();
    }
}
