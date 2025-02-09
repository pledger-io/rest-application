package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.ChangeTransactionDatesCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class ChangeTransactionDatesHandler implements CommandHandler<ChangeTransactionDatesCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public ChangeTransactionDatesHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeTransactionDatesCommand command) {
        log.info("[{}] - Processing transaction book event", command.id());

        entityManager.update(TransactionJournal.class)
                .set("bookDate", command.bookingDate())
                .set("date", command.date())
                .set("interestDate", command.interestDate())
                .fieldEq("id", command.id())
                .execute();
    }

}
