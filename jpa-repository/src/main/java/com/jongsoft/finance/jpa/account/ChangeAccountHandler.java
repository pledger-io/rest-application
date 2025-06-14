package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.ChangeAccountCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresJpa
@Transactional
public class ChangeAccountHandler implements CommandHandler<ChangeAccountCommand> {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final ReactiveEntityManager entityManager;

  @Inject
  ChangeAccountHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(ChangeAccountCommand command) {
    log.info("[{}] - Processing account change event", command.id());

    entityManager
        .update(AccountJpa.class)
        .set("iban", command.iban())
        .set("bic", command.bic())
        .set("number", command.number())
        .fieldEq("id", command.id())
        .execute();
  }
}
