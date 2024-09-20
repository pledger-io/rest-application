package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
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

        entityManager.update()
                .hql("update TransactionJournal set deleted = :now where id = :id")
                .set("id", command.id())
                .set("now", new Date())
                .execute();

        var updateHql = """
                update TransactionJpa
                set deleted = :now
                where journal.id = :id""";

        entityManager.update()
                .hql(updateHql)
                .set("id", command.id())
                .set("now", new Date())
                .execute();
    }

}
