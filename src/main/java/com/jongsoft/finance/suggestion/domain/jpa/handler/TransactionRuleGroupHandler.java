package com.jongsoft.finance.suggestion.domain.jpa.handler;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.core.domain.jpa.query.expression.Expressions;
import com.jongsoft.finance.suggestion.domain.commands.CreateRuleGroupCommand;
import com.jongsoft.finance.suggestion.domain.commands.RenameRuleGroupCommand;
import com.jongsoft.finance.suggestion.domain.commands.ReorderRuleGroupCommand;
import com.jongsoft.finance.suggestion.domain.commands.RuleGroupDeleteCommand;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleGroupJpa;
import com.jongsoft.lang.Collections;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class TransactionRuleGroupHandler {

    private final Logger log = LoggerFactory.getLogger(TransactionRuleGroupHandler.class);
    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    TransactionRuleGroupHandler(
            ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @EventListener
    void handleCreate(CreateRuleGroupCommand command) {
        log.info("[{}] - Processing rule group create event", command.name());

        var currentMax = entityManager
                .from(RuleGroupJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .projectSingleValue(Integer.class, "max(sort)");

        var jpaEntity = new RuleGroupJpa(
                command.name(), currentMax.getOrSupply(() -> 1), entityManager.currentUser());

        entityManager.persist(jpaEntity);
    }

    @EventListener
    void handleRename(RenameRuleGroupCommand command) {
        log.info("[{}] - Processing rule group rename event", command.id());

        entityManager
                .update(RuleGroupJpa.class)
                .set("name", command.name())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    void handleReorder(ReorderRuleGroupCommand command) {
        log.info("[{}] - Processing rule group sorting event", command.id());

        var jpaEntity =
                entityManager.getDetached(RuleGroupJpa.class, Collections.Map("id", command.id()));

        var updateQuery = entityManager
                .update(RuleGroupJpa.class)
                .fieldIn("id", RuleGroupJpa.class, subQuery -> subQuery.project("id")
                        .fieldEq("user.username", authenticationFacade.authenticated()));
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

    @EventListener
    void handleDelete(RuleGroupDeleteCommand command) {
        log.info("[{}] - Processing rule group delete event", command.id());

        entityManager
                .update(RuleGroupJpa.class)
                .set("archived", true)
                .fieldEq("id", command.id())
                .execute();
    }
}
