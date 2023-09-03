package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.ChangeTransactionDatesCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
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

        var hql = """
                update TransactionJournal
                set date = :date,
                    bookDate = :bookDate,
                    interestDate = :interestDate
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("bookDate", command.bookingDate())
                .set("date", command.date())
                .set("interestDate", command.interestDate())
                .execute();
    }

}
