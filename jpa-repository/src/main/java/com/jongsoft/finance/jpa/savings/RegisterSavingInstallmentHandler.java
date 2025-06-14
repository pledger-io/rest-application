package com.jongsoft.finance.jpa.savings;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.query.expression.Expressions;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.savings.RegisterSavingInstallmentCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class RegisterSavingInstallmentHandler
    implements CommandHandler<RegisterSavingInstallmentCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public RegisterSavingInstallmentHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(RegisterSavingInstallmentCommand command) {
    log.info("[{}] - Incrementing allocation for saving goal.", command.id());

    entityManager
        .update(SavingGoalJpa.class)
        .set(
            "allocated",
            Expressions.addition(
                Expressions.field("allocated"), Expressions.value(command.amount())))
        .fieldEq("id", command.id())
        .execute();
  }
}
