package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.query.expression.Expressions;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.ReorderRuleGroupCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class ReorderRuleGroupHandler implements CommandHandler<ReorderRuleGroupCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Inject
    public ReorderRuleGroupHandler(
            ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(ReorderRuleGroupCommand command) {
        log.info("[{}] - Processing rule group sorting event", command.id());

        var jpaEntity =
                entityManager.getDetached(RuleGroupJpa.class, Collections.Map("id", command.id()));

        var updateQuery =
                entityManager
                        .update(RuleGroupJpa.class)
                        .fieldIn(
                                "id",
                                RuleGroupJpa.class,
                                subQuery ->
                                        subQuery.project("id")
                                                .fieldEq(
                                                        "user.username",
                                                        authenticationFacade.authenticated()));
        if ((command.sort() - jpaEntity.getSort()) < 0) {
            updateQuery.fieldBetween("sort", command.sort(), jpaEntity.getSort());
            updateQuery.set(
                    "sort", Expressions.addition(Expressions.field("sort"), Expressions.value(1)));
        } else {
            updateQuery.fieldBetween("sort", jpaEntity.getSort(), command.sort());
            updateQuery.set(
                    "sort", Expressions.addition(Expressions.field("sort"), Expressions.value(-1)));
        }
        updateQuery.execute();

        entityManager
                .update(RuleGroupJpa.class)
                .set("sort", command.sort())
                .fieldEq("id", command.id())
                .execute();
    }
}
