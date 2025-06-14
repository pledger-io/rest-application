package com.jongsoft.finance.jpa.insight;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.commands.insight.CreateSpendingInsight;
import com.jongsoft.finance.providers.SpendingInsightProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
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
@Named("spendingInsightProvider")
public class SpendingInsightProviderJpa implements SpendingInsightProvider {

  private final AuthenticationFacade authenticationFacade;
  private final ReactiveEntityManager entityManager;

  public SpendingInsightProviderJpa(
      AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
    this.authenticationFacade = authenticationFacade;
    this.entityManager = entityManager;
  }

  @Override
  public Sequence<SpendingInsight> lookup() {
    log.trace("Fetching all spending insights");

    return entityManager
        .from(SpendingInsightJpa.class)
        .joinFetch("user")
        .fieldEq("user.username", authenticationFacade.authenticated())
        .stream()
        .map(this::convert)
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
        .map(this::convert);
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
        .map(this::convert)
        .collect(ReactiveEntityManager.sequenceCollector());
  }

  @Override
  public ResultPage<SpendingInsight> lookup(SpendingInsightProvider.FilterCommand filter) {
    log.trace("Fetching spending insights with filter: {}", filter);

    if (filter instanceof SpendingInsightFilterCommand delegate) {
      delegate.user(authenticationFacade.authenticated());
      return entityManager.from(delegate).paged().map(this::convert);
    }

    throw new IllegalStateException("Cannot use non-JPA filter on SpendingInsightProviderJpa");
  }

  @BusinessEventListener
  public void save(CreateSpendingInsight command) {
    log.trace("Saving spending insight: {}", command.spendingInsight());

    // Convert metadata to string values
    var insight = command.spendingInsight();
    Map<String, String> metadata = new HashMap<>();
    for (Map.Entry<String, Object> entry : insight.getMetadata().entrySet()) {
      metadata.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
    }

    // Create the JPA entity
    SpendingInsightJpa jpa =
        SpendingInsightJpa.builder()
            .type(insight.getType())
            .category(insight.getCategory())
            .severity(insight.getSeverity())
            .score(insight.getScore())
            .detectedDate(insight.getDetectedDate())
            .message(insight.getMessage())
            .yearMonth(YearMonth.from(insight.getDetectedDate()))
            .transactionId(insight.getTransactionId())
            .metadata(metadata)
            .user(entityManager.currentUser())
            .build();

    // Save the entity
    entityManager.persist(jpa);
  }

  private SpendingInsight convert(SpendingInsightJpa source) {
    if (source == null) {
      return null;
    }

    // Convert metadata from string values to objects
    Map<String, Object> metadata =
        source.getMetadata().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return SpendingInsight.builder()
        .type(source.getType())
        .score(source.getScore())
        .severity(source.getSeverity())
        .category(source.getCategory())
        .message(source.getMessage())
        .transactionId(source.getTransactionId())
        .metadata(metadata)
        .detectedDate(source.getDetectedDate())
        .build();
  }
}
