package com.jongsoft.finance.suggestion.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.suggestion.adapter.api.TransactionRuleProvider;
import com.jongsoft.finance.suggestion.domain.commands.CreateRuleGroupCommand;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleChangeJpa;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleConditionJpa;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleGroupJpa;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleJpa;
import com.jongsoft.finance.suggestion.domain.model.TransactionRule;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
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
    public Optional<TransactionRule> lookup(long id) {
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

        var ruleJpa = new RuleJpa(
                rule.getName(),
                rule.getDescription(),
                rule.isRestrictive(),
                rule.isActive(),
                sortOrder,
                entityManager.currentUser(),
                group(rule.getGroup()));

        ruleJpa.setConditions(convertConditions(ruleJpa, Collections.List(rule.getConditions()))
                .toJava());
        ruleJpa.setChanges(
                convertChanges(ruleJpa, Collections.List(rule.getChanges())).toJava());

        entityManager.persist(ruleJpa);
    }

    protected TransactionRule convert(RuleJpa source) {
        if (source == null) {
            return null;
        }

        var rule = new TransactionRule(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.isRestrictive(),
                source.isActive(),
                source.isArchived(),
                Control.Option(source.getGroup()).map(RuleGroupJpa::getName).getOrSupply(() -> ""),
                source.getSort(),
                null,
                null);
        source.getConditions()
                .forEach(c -> rule
                .new Condition(c.getId(), c.getField(), c.getOperation(), c.getCondition()));
        source.getChanges().forEach(c -> rule.new Change(c.getId(), c.getField(), c.getValue()));

        return rule;
    }

    private Sequence<RuleChangeJpa> convertChanges(
            RuleJpa rule, Sequence<TransactionRule.Change> changes) {
        return changes.map(c -> new RuleChangeJpa(c.getField(), c.getChange(), rule));
    }

    private Sequence<RuleConditionJpa> convertConditions(
            RuleJpa rule, Sequence<TransactionRule.Condition> conditions) {
        return conditions.map(
                c -> new RuleConditionJpa(c.getField(), c.getOperation(), c.getCondition(), rule));
    }

    private RuleGroupJpa group(String group) {
        return entityManager
                .from(RuleGroupJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("name", group)
                .singleResult()
                .getOrSupply(() -> {
                    CreateRuleGroupCommand.ruleGroupCreated(group);
                    return group(group);
                });
    }
}
