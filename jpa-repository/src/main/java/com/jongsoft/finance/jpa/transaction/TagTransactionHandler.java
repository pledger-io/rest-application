package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.TagTransactionCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresJpa
@Transactional
public class TagTransactionHandler implements CommandHandler<TagTransactionCommand> {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final ReactiveEntityManager entityManager;
  private final AuthenticationFacade authenticationFacade;

  @Inject
  public TagTransactionHandler(
      ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
    this.entityManager = entityManager;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  @BusinessEventListener
  public void handle(TagTransactionCommand command) {
    log.info("[{}] - Processing transaction tagging event", command.id());

    var transaction = entityManager.getById(TransactionJournal.class, command.id());
    transaction.getTags().clear();

    command.tags().map(this::tag).filter(Objects::nonNull).forEach(tag -> transaction
        .getTags()
        .add(tag));

    entityManager.persist(transaction);
  }

  private TagJpa tag(String name) {
    return entityManager
        .from(TagJpa.class)
        .fieldEq("name", name)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .singleResult()
        .getOrThrow(() -> new IllegalArgumentException("tag not found"));
  }
}
