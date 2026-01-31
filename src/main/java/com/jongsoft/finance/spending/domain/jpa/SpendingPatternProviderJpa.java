package com.jongsoft.finance.spending.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.spending.adapter.api.SpendingPatternProvider;
import com.jongsoft.finance.spending.domain.commands.CleanInsightsForMonth;
import com.jongsoft.finance.spending.domain.commands.CreateSpendingPattern;
import com.jongsoft.finance.spending.domain.jpa.entity.SpendingPatternJpa;
import com.jongsoft.finance.spending.domain.jpa.filter.SpendingPatternFilterCommand;
import com.jongsoft.finance.spending.domain.jpa.mapper.SpendingPatternMapper;
import com.jongsoft.finance.spending.domain.model.SpendingPattern;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@ReadOnly
@Singleton
public class SpendingPatternProviderJpa implements SpendingPatternProvider {

    private final Logger log = LoggerFactory.getLogger(SpendingPatternProviderJpa.class);

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final SpendingPatternMapper mapper;

    public SpendingPatternProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            SpendingPatternMapper mapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.mapper = mapper;
    }

    @Override
    public Sequence<SpendingPattern> lookup() {
        log.trace("Fetching all spending patterns");

        return entityManager
                .from(SpendingPatternJpa.class)
                .joinFetch("user")
                .fieldEq("user.username", authenticationFacade.authenticated())
                .stream()
                .map(mapper::toModel)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public Optional<SpendingPattern> lookup(String category) {
        log.trace("Fetching spending pattern for category: {}", category);

        return entityManager
                .from(SpendingPatternJpa.class)
                .joinFetch("user")
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("category", category)
                .singleResult()
                .map(mapper::toModel);
    }

    @Override
    public Sequence<SpendingPattern> lookup(YearMonth yearMonth) {
        log.trace("Fetching spending patterns for year-month: {}", yearMonth);

        return entityManager
                .from(SpendingPatternJpa.class)
                .joinFetch("user")
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("yearMonth", yearMonth.toString())
                .stream()
                .map(mapper::toModel)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public ResultPage<SpendingPattern> lookup(FilterCommand filter) {
        log.trace("Fetching spending patterns with filter: {}", filter);

        if (filter instanceof SpendingPatternFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());
            return entityManager.from(delegate).paged().map(mapper::toModel);
        }

        throw new IllegalStateException("Cannot use non-JPA filter on SpendingPatternProviderJpa");
    }

    @EventListener
    public void save(CreateSpendingPattern command) {
        log.trace("Saving spending pattern: {}", command);

        // Convert metadata to string values
        Map<String, String> metadata = new HashMap<>();
        for (Map.Entry<String, ?> entry : command.metadata().entrySet()) {
            metadata.put(
                    entry.getKey(),
                    Control.Option(entry.getValue()).map(Object::toString).getOrSupply(() -> null));
        }

        // Create the JPA entity
        SpendingPatternJpa entity = new SpendingPatternJpa(
                command.type(),
                command.category(),
                command.confidence(),
                command.detectedDate(),
                YearMonth.from(command.detectedDate()),
                metadata,
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
                        "DELETE FROM SpendingPatternJpa WHERE yearMonth = :yearMonth and user.id in (select id from UserAccountJpa a where a.username = :username)")
                .setParameter("yearMonth", command.month().toString())
                .setParameter("username", authenticationFacade.authenticated())
                .executeUpdate();
    }
}
