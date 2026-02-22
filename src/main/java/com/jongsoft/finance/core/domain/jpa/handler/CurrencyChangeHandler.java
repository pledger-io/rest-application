package com.jongsoft.finance.core.domain.jpa.handler;

import com.jongsoft.finance.core.domain.commands.ChangeCurrencyPropertyCommand;
import com.jongsoft.finance.core.domain.commands.CreateCurrencyCommand;
import com.jongsoft.finance.core.domain.commands.RenameCurrencyCommand;
import com.jongsoft.finance.core.domain.jpa.entity.CurrencyJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

@Singleton
@Transactional
class CurrencyChangeHandler {

    private final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CurrencyChangeHandler.class);

    private final ReactiveEntityManager entityManager;

    CurrencyChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handlePropertyChange(ChangeCurrencyPropertyCommand<?> command) {
        log.trace("[{}] - Processing currency property {} event", command.code(), command.type());

        var currency = entityManager.update(CurrencyJpa.class);
        switch (command.type()) {
            case ENABLED -> currency.set("enabled", command.value());
            case DECIMAL_PLACES -> currency.set("decimalPlaces", command.value());
        }

        currency.fieldEq("code", command.code()).execute();
    }

    @EventListener
    public void handleCreate(CreateCurrencyCommand command) {
        log.info("[{}] - Processing currency create event", command.isoCode());

        var entity = new CurrencyJpa(
                null, command.name(), command.symbol(), command.isoCode(), 2, true, false);

        entityManager.persist(entity);
    }

    @EventListener
    public void handleRename(RenameCurrencyCommand command) {
        log.info("[{}] - Processing currency rename event", command.id());

        entityManager
                .update(CurrencyJpa.class)
                .set("name", command.name())
                .set("code", command.isoCode())
                .set("symbol", command.symbol())
                .fieldEq("id", command.id())
                .execute();
    }
}
