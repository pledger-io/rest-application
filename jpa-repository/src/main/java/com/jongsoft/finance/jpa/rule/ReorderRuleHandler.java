package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.query.expression.Expressions;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.ReorderRuleCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class ReorderRuleHandler implements CommandHandler<ReorderRuleCommand> {

  private final ReactiveEntityManager entityManager;
  private final AuthenticationFacade authenticationFacade;

  @Inject
  public ReorderRuleHandler(
      ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
    this.entityManager = entityManager;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  @BusinessEventListener
  public void handle(ReorderRuleCommand command) {
    log.info("[{}] - Processing transaction rule sort event", command.id());

    var jpaEntity = entityManager.getDetached(RuleJpa.class, Collections.Map("id", command.id()));

    var updateQuery = entityManager
        .update(RuleJpa.class)
        .fieldIn("id", RuleJpa.class, subQuery -> subQuery
            .project("id")
            .fieldEq("user.username", authenticationFacade.authenticated())
            .fieldEq("group.name", jpaEntity.getGroup().getName()));

    if ((command.sort() - jpaEntity.getSort()) < 0) {
      updateQuery
          .set("sort", Expressions.addition(Expressions.field("sort"), Expressions.value(1)))
          .fieldBetween("sort", command.sort(), jpaEntity.getSort());
    } else {
      updateQuery
          .set("sort", Expressions.addition(Expressions.field("sort"), Expressions.value(-1)))
          .fieldBetween("sort", jpaEntity.getSort(), command.sort());
    }
    updateQuery.execute();

    entityManager
        .update(RuleJpa.class)
        .set("sort", command.sort())
        .fieldEq("id", command.id())
        .execute();
  }
}
