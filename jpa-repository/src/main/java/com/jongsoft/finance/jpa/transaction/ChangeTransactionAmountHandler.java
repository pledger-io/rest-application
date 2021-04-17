package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.ChangeTransactionAmountCommand;
import com.jongsoft.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class ChangeTransactionAmountHandler implements CommandHandler<ChangeTransactionAmountCommand> {

    private final ReactiveEntityManager entityManager;

    public ChangeTransactionAmountHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeTransactionAmountCommand command) {
        log.trace("[{}] - Processing transaction amount change event", command.id());

        var hql = """
                update TransactionJpa 
                set amount = case when amount >= 0 
                                then :amount 
                                else :negAmount end
                where journal.id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("amount", command.amount())
                .set("negAmount", command.amount().negate())
                .execute();

        var hqlTransaction = """
                update TransactionJournal 
                set currency = :currency
                where id = :id""";

        entityManager.update()
                .hql(hqlTransaction)
                .set("id", command.id())
                .set("currency", entityManager.get(CurrencyJpa.class, Collections.Map("code", command.currency())))
                .execute();
    }

}
