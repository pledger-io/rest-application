package com.jongsoft.finance.jpa.account.handler;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.account.entity.AccountTypeJpa;
import com.jongsoft.finance.jpa.core.entity.CurrencyJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.RenameAccountCommand;
import com.jongsoft.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class RenameAccountHandler implements CommandHandler<RenameAccountCommand> {

    private final ReactiveEntityManager entityManager;

    public RenameAccountHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RenameAccountCommand command) {
        log.trace("[{}] - Processing account rename event", command.id());

        var hql = """
                update AccountJpa
                set name = :name,
                    description = :description,
                    type = :type,
                    currency = :currency
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("name", command.name())
                .set("description", command.description())
                .set("type", entityManager.get(
                        AccountTypeJpa.class,
                        Collections.Map("label", command.type())))
                .set("currency", entityManager.get(
                        CurrencyJpa.class,
                        Collections.Map("code", command.currency())))
                .set("id", command.id())
                .update();
    }

}
