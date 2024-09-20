package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.ChangeTransactionPartAccount;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class ChangeTransactionPartAccountHandler implements CommandHandler<ChangeTransactionPartAccount> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public ChangeTransactionPartAccountHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeTransactionPartAccount command) {
        log.info("[{}] - Processing transaction account change", command.id());

        var hql = """
                update TransactionJpa
                set account.id = :accountId
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("accountId", command.accountId())
                .execute();
    }

}
