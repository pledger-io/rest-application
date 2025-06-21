package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.SplitTransactionCommand;
import com.jongsoft.lang.Collections;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class SplitTransactionHandler implements CommandHandler<SplitTransactionCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public SplitTransactionHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(SplitTransactionCommand command) {
    log.info("[{}] - Processing transaction split event", command.id());

    var transaction =
        entityManager.getDetached(TransactionJournal.class, Collections.Map("id", command.id()));

    var survivors = command.split().map(Transaction.Part::getId).reject(Objects::isNull);

    // Mark all old parts as deleted
    var deletedIds = Collections.List(transaction.getTransactions())
        .reject(t -> survivors.contains(t.getId()))
        .map(TransactionJpa::getId);

    entityManager
        .update(TransactionJpa.class)
        .set("deleted", new Date())
        .fieldEqOneOf("id", deletedIds.stream().toArray())
        .execute();

    // Add new parts
    command
        .split()
        .filter(part -> part.getId() == null)
        .map(part -> TransactionJpa.builder()
            // todo change to native BigDecimal later on
            .amount(BigDecimal.valueOf(part.getAmount()))
            .description(part.getDescription())
            .account(entityManager.getById(AccountJpa.class, part.getAccount().getId()))
            .journal(transaction)
            .build())
        .forEach(entityPart -> {
          transaction.getTransactions().add(entityPart);
          entityManager.persist(entityPart);
        });

    // Update existing parts
    command.split().filter(part -> Objects.nonNull(part.getId())).forEach(part -> entityManager
        .update(TransactionJpa.class)
        .set("amount", part.getAmount())
        .fieldEq("id", part.getId())
        .execute());
  }
}
