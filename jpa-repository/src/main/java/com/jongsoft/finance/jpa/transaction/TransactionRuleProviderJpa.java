package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleGroupCreatedEvent;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.transaction.entity.RuleChangeJpa;
import com.jongsoft.finance.jpa.transaction.entity.RuleConditionJpa;
import com.jongsoft.finance.jpa.transaction.entity.RuleGroupJpa;
import com.jongsoft.finance.jpa.transaction.entity.RuleJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Optional;

@Singleton
@Transactional
@Named("transactionRuleProvider")
public class TransactionRuleProviderJpa implements TransactionRuleProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    public TransactionRuleProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<TransactionRule> lookup() {
        var hql = """
                select r from RuleJpa r
                where r.user.username = :username
                 and r.archived = false
                 order by r.group.sort ASC, r.sort ASC""";

        return entityManager.<RuleJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .sequence()
                .map(this::convert);
    }

    @Override
    public com.jongsoft.lang.control.Optional<TransactionRule> lookup(long id) {
        return entityManager.<RuleJpa>blocking()
                .hql("from RuleJpa where id = :id and user.username = :username")
                .set("id", id)
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .map(this::convert);
    }

    @Override
    public Flowable<TransactionRule> lookup(String group) {
        var hql = """
                select r from RuleJpa r
                where r.user.username = :username
                 and r.group.name = :name
                 and r.archived = false
                order by r.sort asc""";

        return entityManager.<RuleJpa>reactive()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", group)
                .flow()
                .map(this::convert);
    }

    @Override
    @Transactional
    public void save(TransactionRule rule) {
        int sortOrder= rule.getSort();
        if (rule.getId() == null || rule.getSort() == 0) {
            var hql = """
                select max(sort) + 1 from RuleJpa 
                where user.username = :username and archived = false and group.name = :group""";

            sortOrder = entityManager.<Integer>blocking()
                    .hql(hql)
                    .set("username", authenticationFacade.authenticated())
                    .set("group", rule.getGroup())
                    .maybe()
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

        ruleJpa.setConditions(convertConditions(ruleJpa, API.List(rule.getConditions())).toJava());
        ruleJpa.setChanges(convertChanges(ruleJpa, API.List(rule.getChanges())).toJava());

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
                .user(
                        UserAccount.builder()
                                .id(source.getUser().getId())
                                .username(source.getUser().getUsername())
                                .build())
                .description(source.getDescription())
                .active(source.isActive())
                .group(Optional.ofNullable(source.getGroup()).map(RuleGroupJpa::getName).orElse(null))
                .sort(source.getSort())
                .build();

        source.getConditions().forEach(c -> rule.new Condition(c.getId(), c.getField(), c.getOperation(), c.getCondition()));
        source.getChanges().forEach(c -> rule.new Change(c.getId(), c.getField(), c.getValue()));

        return rule;
    }

    private Sequence<RuleChangeJpa> convertChanges(RuleJpa rule, Sequence<TransactionRule.Change> changes) {
        return changes.map(c -> RuleChangeJpa.builder()
                .id(c.getId())
                .rule(rule)
                .field(c.getField())
                .value(c.getChange())
                .build());
    }

    private Sequence<RuleConditionJpa> convertConditions(RuleJpa rule, Sequence<TransactionRule.Condition> conditions) {
        return conditions.map(c -> RuleConditionJpa.builder()
                .id(c.getId())
                .field(c.getField())
                .rule(rule)
                .operation(c.getOperation())
                .condition(c.getCondition())
                .build());
    }

    private UserAccountJpa activeUser() {
        return entityManager.<UserAccountJpa>blocking()
                .hql("from UserAccountJpa where username = :username")
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .get();
    }

    private RuleGroupJpa group(String group) {
        return entityManager.<RuleGroupJpa>reactive()
                .hql("from RuleGroupJpa where user.username = :username and name = :group")
                .set("username", authenticationFacade.authenticated())
                .set("group", group)
                .maybe()
                .switchIfEmpty(Single.create(emitter -> {
                    EventBus.getBus().send(new TransactionRuleGroupCreatedEvent(
                            this,
                            group));

                    emitter.onSuccess(group(group));
                }))
                .blockingGet();
    }
}
