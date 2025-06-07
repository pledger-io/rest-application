package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.query.expression.Expressions;
import com.jongsoft.finance.jpa.query.expression.FieldEquation;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.ChangeTransactionAmountCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class ChangeTransactionAmountHandler
    implements CommandHandler<ChangeTransactionAmountCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public ChangeTransactionAmountHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(ChangeTransactionAmountCommand command) {
    log.info("[{}] - Processing transaction amount change event", command.id());

    entityManager
        .update(TransactionJpa.class)
        .set(
            "amount",
            Expressions.caseWhen(
                Expressions.fieldCondition(null, "amount", FieldEquation.GTE, 0),
                Expressions.value(command.amount()),
                Expressions.value(command.amount().negate())))
        .fieldEq("journal.id", command.id())
        .execute();

    entityManager
        .update(TransactionJournal.class)
        .set(
            "currency",
            entityManager
                .from(CurrencyJpa.class)
                .fieldEq("code", command.currency())
                .singleResult()
                .get())
        .fieldEq("id", command.id())
        .execute();
  }
}
