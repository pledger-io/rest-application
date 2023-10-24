package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.DescribeTransactionCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class DescribeTransactionHandler implements CommandHandler<DescribeTransactionCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public DescribeTransactionHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(DescribeTransactionCommand command) {
        log.info("[{}] - Processing transaction describe event", command.id());
        var hqlTransaction = """
                update TransactionJournal 
                set description = :description
                where id = :id""";

        entityManager.update()
                .hql(hqlTransaction)
                .set("id", command.id())
                .set("description", command.description())
                .execute();
    }
}
