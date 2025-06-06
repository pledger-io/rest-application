package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.ChangeRuleCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class ChangeRuleHandler implements CommandHandler<ChangeRuleCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public ChangeRuleHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(ChangeRuleCommand command) {
    log.info("[{}] - Updating rule change", command.id());

    entityManager
        .update(RuleChangeJpa.class)
        .set("field", command.column())
        .set("`value`", command.change())
        .fieldEq("id", command.id())
        .execute();
  }
}
