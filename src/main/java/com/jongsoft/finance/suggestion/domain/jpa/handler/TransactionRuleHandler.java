package com.jongsoft.finance.suggestion.domain.jpa.handler;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.core.domain.jpa.query.expression.Expressions;
import com.jongsoft.finance.suggestion.domain.commands.ChangeConditionCommand;
import com.jongsoft.finance.suggestion.domain.commands.ChangeRuleCommand;
import com.jongsoft.finance.suggestion.domain.commands.ReorderRuleCommand;
import com.jongsoft.finance.suggestion.domain.commands.RuleRemovedCommand;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleChangeJpa;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleConditionJpa;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleJpa;
import com.jongsoft.lang.Collections;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class TransactionRuleHandler {

    private final Logger log = LoggerFactory.getLogger(TransactionRuleHandler.class);
    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    TransactionRuleHandler(
            ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @EventListener
    void handleConditionChange(ChangeConditionCommand command) {
        log.info("[{}] - Updating rule condition", command.id());

        entityManager
                .update(RuleConditionJpa.class)
                .set("operation", command.operation())
                .set("condition", command.condition())
                .set("field", command.field())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    void handleChangeChange(ChangeRuleCommand command) {
        log.info("[{}] - Updating rule change", command.id());

        entityManager
                .update(RuleChangeJpa.class)
                .set("field", command.column())
                .set("`value`", command.change())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    void handleReorder(ReorderRuleCommand command) {
        log.info("[{}] - Processing transaction rule sort event", command.id());

        var jpaEntity =
                entityManager.getDetached(RuleJpa.class, Collections.Map("id", command.id()));

        var updateQuery = entityManager
                .update(RuleJpa.class)
                .fieldIn("id", RuleJpa.class, subQuery -> subQuery.project("id")
                        .fieldEq("user.username", authenticationFacade.authenticated())
                        .fieldEq("group.name", jpaEntity.getGroup().getName()));

        if ((command.sort() - jpaEntity.getSort()) < 0) {
            updateQuery
                    .set(
                            "sort",
                            Expressions.addition(Expressions.field("sort"), Expressions.value(1)))
                    .fieldBetween("sort", command.sort(), jpaEntity.getSort());
        } else {
            updateQuery
                    .set(
                            "sort",
                            Expressions.addition(Expressions.field("sort"), Expressions.value(-1)))
                    .fieldBetween("sort", jpaEntity.getSort(), command.sort());
        }
        updateQuery.execute();

        entityManager
                .update(RuleJpa.class)
                .set("sort", command.sort())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    void handleDelete(RuleRemovedCommand command) {
        log.info("[{}] - Processing rule delete event.", command.ruleId());

        entityManager
                .update(RuleJpa.class)
                .set("archived", true)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("id", command.ruleId())
                .execute();
    }
}
