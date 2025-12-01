package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.DescribeTransactionCommand;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
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

        entityManager
                .update(TransactionJournal.class)
                .set("description", command.description())
                .fieldEq("id", command.id())
                .execute();
    }
}
