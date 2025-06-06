package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.ChangeContractCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class ChangeContractHandler implements CommandHandler<ChangeContractCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public ChangeContractHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(ChangeContractCommand command) {
    log.info("[{}] - Processing contract changed event", command.id());

    entityManager
        .update(ContractJpa.class)
        .set("name", command.name())
        .set("startDate", command.start())
        .set("endDate", command.end())
        .set("description", command.description())
        .fieldEq("id", command.id())
        .execute();
  }
}
