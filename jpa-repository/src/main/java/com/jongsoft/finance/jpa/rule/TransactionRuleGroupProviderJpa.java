package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ReadOnly
@Singleton
@Named("transactionRuleGroupProvider")
public class TransactionRuleGroupProviderJpa implements TransactionRuleGroupProvider {

  private final AuthenticationFacade authenticationFacade;
  private final ReactiveEntityManager entityManager;

  @Inject
  public TransactionRuleGroupProviderJpa(
      AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
    this.authenticationFacade = authenticationFacade;
    this.entityManager = entityManager;
  }

  @Override
  public Sequence<TransactionRuleGroup> lookup() {
    log.trace("TransactionRuleGroup listing");

    return entityManager
        .from(RuleGroupJpa.class)
        .fieldEq("archived", false)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .stream()
        .map(this::convert)
        .collect(ReactiveEntityManager.sequenceCollector());
  }

  @Override
  public Optional<TransactionRuleGroup> lookup(String name) {
    log.trace("TransactionRuleGroup lookup with name: {}", name);

    return entityManager
        .from(RuleGroupJpa.class)
        .fieldEq("archived", false)
        .fieldEq("name", name)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .singleResult()
        .map(this::convert);
  }

  private TransactionRuleGroup convert(RuleGroupJpa source) {
    if (source == null) {
      return null;
    }

    return TransactionRuleGroup.builder()
        .id(source.getId())
        .name(source.getName())
        .sort(source.getSort())
        .build();
  }
}
