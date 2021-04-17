package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.DeleteTransactionCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Date;

@Slf4j
@Singleton
@Transactional
public class DeleteTransactionHandler implements CommandHandler<DeleteTransactionCommand> {

    private final ReactiveEntityManager entityManager;

    public DeleteTransactionHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(DeleteTransactionCommand command) {
        log.trace("[{}] - Processing transaction delete event", command.id());

        entityManager.update()
                .hql("update TransactionJournal set deleted = :now where id = :id")
                .set("id", command.id())
                .set("now", new Date())
                .execute();

        var updateHql = """
                update TransactionJpa
                set deleted = :now
                where journal_id = :id""";

        entityManager.update()
                .hql(updateHql)
                .set("id", command.id())
                .set("now", new Date())
                .execute();
    }

}
