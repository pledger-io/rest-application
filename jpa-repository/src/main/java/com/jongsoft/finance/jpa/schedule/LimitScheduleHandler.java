package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.LimitScheduleCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class LimitScheduleHandler implements CommandHandler<LimitScheduleCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public LimitScheduleHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(LimitScheduleCommand command) {
    log.info("[{}] - Processing schedule limit event", command.id());

    entityManager
        .update(ScheduledTransactionJpa.class)
        .set("start", command.start())
        .set("end", command.end())
        .fieldEq("id", command.id())
        .execute();
  }
}
