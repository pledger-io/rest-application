package com.jongsoft.finance.jpa.currency;

import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.currency.ChangeCurrencyPropertyCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class ChangeCurrencyPropertyHandler implements CommandHandler<ChangeCurrencyPropertyCommand<?>> {

    private final ReactiveEntityManager entityManager;

    public ChangeCurrencyPropertyHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void handle(ChangeCurrencyPropertyCommand<?> command) {
        log.trace("[{}] - Processing currency property {} event", command.code(), command.type());

        var updatePart = switch (command.type()) {
            case ENABLED -> " c.enabled = :value";
            case DECIMAL_PLACES -> " c.decimalPlaces = :value";
        };

        var hql = "update CurrencyJpa c set"
                + updatePart
                + " where c.code = :code";

        entityManager.update()
                .hql(hql)
                .set("code", command.code())
                .set("value", command.value())
                .update();
    }

}
