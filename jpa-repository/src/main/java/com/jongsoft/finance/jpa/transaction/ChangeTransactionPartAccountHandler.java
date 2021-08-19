package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.ChangeTransactionPartAccount;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ChangeTransactionPartAccountHandler implements CommandHandler<ChangeTransactionPartAccount> {

    private final ReactiveEntityManager entityManager;

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
