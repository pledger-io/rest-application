package com.jongsoft.finance.jpa.currency;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.currency.ChangeCurrencyPropertyCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
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

        var currency = entityManager.update(CurrencyJpa.class);
        switch (command.type()) {
            case ENABLED -> currency.set("enabled", command.value());
            case DECIMAL_PLACES -> currency.set("decimalPlaces", command.value());
        };
        currency.fieldEq("code", command.code())
                .execute();
    }

}
