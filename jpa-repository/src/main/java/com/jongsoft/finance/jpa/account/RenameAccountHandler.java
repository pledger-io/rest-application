package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.RenameAccountCommand;
import com.jongsoft.lang.Collections;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresJpa
@Transactional
public class RenameAccountHandler implements CommandHandler<RenameAccountCommand> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ReactiveEntityManager entityManager;

    @Inject
    RenameAccountHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RenameAccountCommand command) {
        log.info("[{}] - Processing account rename event", command.id());

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
                .execute();
    }

}
