package com.jongsoft.finance.jpa.tag;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.DeleteTagCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class DeleteTagHandler implements CommandHandler<DeleteTagCommand> {

  private final ReactiveEntityManager entityManager;
  private final AuthenticationFacade authenticationFacade;

  public DeleteTagHandler(
      ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
    this.entityManager = entityManager;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  @BusinessEventListener
  public void handle(DeleteTagCommand command) {
    log.info("[{}] - Processing tag deletion event", command.tag());

    entityManager
        .update(TagJpa.class)
        .set("archived", true)
        .fieldEq("name", command.tag())
        .fieldEq("user.username", authenticationFacade.authenticated())
        .execute();
  }
}
