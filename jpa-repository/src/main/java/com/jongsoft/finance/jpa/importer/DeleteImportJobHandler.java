package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.importer.DeleteImportJobCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class DeleteImportJobHandler implements CommandHandler<DeleteImportJobCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public DeleteImportJobHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(DeleteImportJobCommand command) {
    log.info("[{}] - Processing import deleted event", command.id());

    entityManager
        .update(ImportJpa.class)
        .set("archived", true)
        .fieldEq("id", command.id())
        .execute();
  }
}
