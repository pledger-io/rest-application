package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.rule.CreateRuleGroupCommand;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
@RequiresJpa
@Named("transactionRuleProvider")
public class TransactionRuleProviderJpa implements TransactionRuleProvider {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final AuthenticationFacade authenticationFacade;
  private final ReactiveEntityManager entityManager;

  @Inject
  public TransactionRuleProviderJpa(
      AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
    this.authenticationFacade = authenticationFacade;
    this.entityManager = entityManager;
  }

  @Override
  public Sequence<TransactionRule> lookup() {
    logger.trace("Listing all transaction rules.");

    return entityManager
        .from(RuleJpa.class)
        .joinFetch("user")
        .fieldEq("user.username", authenticationFacade.authenticated())
        .fieldEq("archived", false)
        .orderBy("sort", true)
        .stream()
        .map(this::convert)
        .collect(ReactiveEntityManager.sequenceCollector());
  }

  @Override
  public com.jongsoft.lang.control.Optional<TransactionRule> lookup(long id) {
    logger.trace("Looking up transaction rule with id {}.", id);

    return entityManager
        .from(RuleJpa.class)
        .fieldEq("id", id)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .singleResult()
        .map(this::convert);
  }

  @Override
  public Sequence<TransactionRule> lookup(String group) {
    logger.trace("Listing all transaction rules in group {}.", group);

    return entityManager
        .from(RuleJpa.class)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .fieldEq("group.name", group)
        .fieldEq("archived", false)
        .orderBy("sort", true)
        .stream()
        .map(this::convert)
        .collect(ReactiveEntityManager.sequenceCollector());
  }

  @Override
  public void save(TransactionRule rule) {
    int sortOrder = rule.getSort();
    if (rule.getId() == null || rule.getSort() == 0) {
      sortOrder = entityManager
          .from(RuleJpa.class)
          .fieldEq("user.username", authenticationFacade.authenticated())
          .fieldEq("group.name", rule.getGroup())
          .projectSingleValue(Integer.class, "max(sort)")
          .getOrSupply(() -> 1);
    }

    var ruleJpa = RuleJpa.builder()
        .id(rule.getId())
        .name(rule.getName())
        .description(rule.getDescription())
        .restrictive(rule.isRestrictive())
        .active(rule.isActive())
        .user(activeUser())
        .archived(rule.isDeleted())
        .group(group(rule.getGroup()))
        .sort(sortOrder)
        .build();

    ruleJpa.setConditions(
        convertConditions(ruleJpa, Collections.List(rule.getConditions())).toJava());
    ruleJpa.setChanges(
        convertChanges(ruleJpa, Collections.List(rule.getChanges())).toJava());

    entityManager.persist(ruleJpa);
  }

  protected TransactionRule convert(RuleJpa source) {
    if (source == null) {
      return null;
    }

    var rule = TransactionRule.builder()
        .id(source.getId())
        .name(source.getName())
        .restrictive(source.isRestrictive())
        .user(UserAccount.builder()
            .id(source.getUser().getId())
            .username(new UserIdentifier(source.getUser().getUsername()))
            .build())
        .description(source.getDescription())
        .active(source.isActive())
        .group(Optional.ofNullable(source.getGroup()).map(RuleGroupJpa::getName).orElse(null))
        .sort(source.getSort())
        .build();

    source
        .getConditions()
        .forEach(
            c -> rule.new Condition(c.getId(), c.getField(), c.getOperation(), c.getCondition()));
    source.getChanges().forEach(c -> rule.new Change(c.getId(), c.getField(), c.getValue()));

    return rule;
  }

  private Sequence<RuleChangeJpa> convertChanges(
      RuleJpa rule, Sequence<TransactionRule.Change> changes) {
    return changes.map(c -> RuleChangeJpa.builder()
        .id(c.getId())
        .rule(rule)
        .field(c.getField())
        .value(c.getChange())
        .build());
  }

  private Sequence<RuleConditionJpa> convertConditions(
      RuleJpa rule, Sequence<TransactionRule.Condition> conditions) {
    return conditions.map(c -> RuleConditionJpa.builder()
        .id(c.getId())
        .field(c.getField())
        .rule(rule)
        .operation(c.getOperation())
        .condition(c.getCondition())
        .build());
  }

  private UserAccountJpa activeUser() {
    return entityManager
        .from(UserAccountJpa.class)
        .fieldEq("username", authenticationFacade.authenticated())
        .singleResult()
        .get();
  }

  private RuleGroupJpa group(String group) {
    return entityManager
        .from(RuleGroupJpa.class)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .fieldEq("name", group)
        .singleResult()
        .getOrSupply(() -> {
          EventBus.getBus().send(new CreateRuleGroupCommand(group));
          return group(group);
        });
  }
}
