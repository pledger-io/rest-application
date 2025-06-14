package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.WarnBeforeExpiryCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class WarnBeforeExpiryHandler implements CommandHandler<WarnBeforeExpiryCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public WarnBeforeExpiryHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(WarnBeforeExpiryCommand command) {
    log.info("[{}] - Processing contract warning event", command.id());

    entityManager
        .update(ContractJpa.class)
        .set("warningActive", true)
        .set("endDate", command.endDate())
        .fieldEq("id", command.id())
        .execute();
  }
}
