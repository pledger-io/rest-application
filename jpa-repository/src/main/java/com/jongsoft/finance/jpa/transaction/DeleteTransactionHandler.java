package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.DeleteTransactionCommand;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class DeleteTransactionHandler implements CommandHandler<DeleteTransactionCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public DeleteTransactionHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(DeleteTransactionCommand command) {
        log.info("[{}] - Processing transaction delete event", command.id());

        entityManager
                .update(TransactionJournal.class)
                .set("deleted", new Date())
                .fieldEq("id", command.id())
                .execute();

        entityManager
                .update(TransactionJpa.class)
                .set("deleted", new Date())
                .fieldEq("journal.id", command.id())
                .execute();
    }
}
