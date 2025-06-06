package com.jongsoft.finance.jpa.category;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.category.DeleteCategoryCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class DeleteCategoryHandler implements CommandHandler<DeleteCategoryCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public DeleteCategoryHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(DeleteCategoryCommand command) {
    log.info("[{}] - Processing remove event for category", command.id());

    entityManager
        .update(CategoryJpa.class)
        .set("archived", true)
        .fieldEq("id", command.id())
        .execute();
  }
}
