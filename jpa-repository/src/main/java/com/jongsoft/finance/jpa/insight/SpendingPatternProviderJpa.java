package com.jongsoft.finance.jpa.insight;

import static com.jongsoft.finance.jpa.insight.SpendingPatternJpa.*;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.commands.insight.CleanInsightsForMonth;
import com.jongsoft.finance.messaging.commands.insight.CreateSpendingPattern;
import com.jongsoft.finance.providers.SpendingPatternProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ReadOnly
@Singleton
@RequiresJpa
@Named("spendingPatternProvider")
public class SpendingPatternProviderJpa implements SpendingPatternProvider {

  private final AuthenticationFacade authenticationFacade;
  private final ReactiveEntityManager entityManager;

  public SpendingPatternProviderJpa(
      AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
    this.authenticationFacade = authenticationFacade;
    this.entityManager = entityManager;
  }

  @Override
  public Sequence<SpendingPattern> lookup() {
    log.trace("Fetching all spending patterns");

    return entityManager
        .from(SpendingPatternJpa.class)
        .joinFetch("user")
        .fieldEq(COLUMN_USERNAME, authenticationFacade.authenticated())
        .stream()
        .map(this::convert)
        .collect(ReactiveEntityManager.sequenceCollector());
  }

  @Override
  public Optional<SpendingPattern> lookup(String category) {
    log.trace("Fetching spending pattern for category: {}", category);

    return entityManager
        .from(SpendingPatternJpa.class)
        .joinFetch("user")
        .fieldEq(COLUMN_USERNAME, authenticationFacade.authenticated())
        .fieldEq(COLUMN_CATEGORY, category)
        .singleResult()
        .map(this::convert);
  }

  @Override
  public Sequence<SpendingPattern> lookup(YearMonth yearMonth) {
    log.trace("Fetching spending patterns for year-month: {}", yearMonth);

    return entityManager
        .from(SpendingPatternJpa.class)
        .joinFetch("user")
        .fieldEq(COLUMN_USERNAME, authenticationFacade.authenticated())
        .fieldEq(COLUMN_YEAR_MONTH, yearMonth.toString())
        .stream()
        .map(this::convert)
        .collect(ReactiveEntityManager.sequenceCollector());
  }

  @Override
  public ResultPage<SpendingPattern> lookup(FilterCommand filter) {
    log.trace("Fetching spending patterns with filter: {}", filter);

    if (filter instanceof SpendingPatternFilterCommand delegate) {
      delegate.user(authenticationFacade.authenticated());
      return entityManager.from(delegate).paged().map(this::convert);
    }

    throw new IllegalStateException("Cannot use non-JPA filter on SpendingPatternProviderJpa");
  }

  @BusinessEventListener
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
    SpendingPatternJpa jpa =
        SpendingPatternJpa.builder()
            .type(command.type())
            .category(command.category())
            .confidence(command.confidence())
            .detectedDate(command.detectedDate())
            .yearMonth(YearMonth.from(command.detectedDate()))
            .metadata(metadata)
            .user(entityManager.currentUser())
            .build();

    // Save the entity
    entityManager.persist(jpa);
  }

  @BusinessEventListener
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

  private SpendingPattern convert(SpendingPatternJpa source) {
    if (source == null) {
      return null;
    }

    // Convert metadata from string values to objects
    Map<String, Object> metadata =
        source.getMetadata().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return SpendingPattern.builder()
        .type(source.getType())
        .category(source.getCategory())
        .metadata(metadata)
        .confidence(source.getConfidence())
        .detectedDate(source.getDetectedDate())
        .build();
  }
}
