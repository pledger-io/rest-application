package com.jongsoft.finance.jpa.currency;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.currency.ChangeCurrencyPropertyCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ChangeCurrencyPropertyHandler implements CommandHandler<ChangeCurrencyPropertyCommand<?>> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public ChangeCurrencyPropertyHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
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
                .execute();
    }

}
