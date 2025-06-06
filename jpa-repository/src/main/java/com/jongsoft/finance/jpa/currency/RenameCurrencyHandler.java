package com.jongsoft.finance.jpa.currency;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.currency.RenameCurrencyCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class RenameCurrencyHandler implements CommandHandler<RenameCurrencyCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public RenameCurrencyHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(RenameCurrencyCommand command) {
    log.info("[{}] - Processing currency rename event", command.id());

    entityManager
        .update(CurrencyJpa.class)
        .set("name", command.name())
        .set("code", command.isoCode())
        .set("symbol", command.symbol())
        .fieldEq("id", command.id())
        .execute();
  }
}
