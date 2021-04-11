package com.jongsoft.finance.jpa.core;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.account.events.CurrencyCreatedEvent;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.providers.CurrencyProvider;
import com.jongsoft.finance.domain.core.events.CurrencyPropertyEvent;
import com.jongsoft.finance.domain.core.events.CurrencyRenameEvent;
import com.jongsoft.finance.jpa.core.entity.CurrencyJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class CurrencyProviderJpa implements CurrencyProvider {

    private final ReactiveEntityManager entityManager;

    public CurrencyProviderJpa(
            ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Currency> lookup(long id) {
        return entityManager.<CurrencyJpa>blocking()
                .hql("from CurrencyJpa where id = :id")
                .set("id", id)
                .maybe()
                .map(this::convert);
    }

    @Override
    public Maybe<Currency> lookup(String code) {
        log.trace("Currency lookup by code: {}", code);

        var hql = """
                select c from CurrencyJpa c 
                where c.archived = false
                    and c.code = :code""";

        return entityManager.<CurrencyJpa>reactive()
                .hql(hql)
                .set("code", code)
                .maybe()
                .map(this::convert);
    }

    @Override
    public Sequence<Currency> lookup() {
        log.trace("Currency listing");

        var hql = """
                select c from CurrencyJpa c 
                where c.archived = false""";

        return entityManager.<CurrencyJpa>blocking()
                .hql(hql)
                .sequence()
                .map(this::convert);
    }

    @Transactional
    @BusinessEventListener
    public void handleCreated(CurrencyCreatedEvent event) {
        log.trace("[{}] - Processing currency create event", event.getCode());

        var entity = CurrencyJpa.builder()
                .name(event.getName())
                .code(event.getCode())
                .symbol(event.getSymbol())
                .enabled(true)
                .decimalPlaces(2)
                .build();

        entityManager.persist(entity);
    }

    @Transactional
    @BusinessEventListener
    public void handleRenameEvent(CurrencyRenameEvent event) {
        log.trace("[{}] - Processing currency rename event", event.getId());

        var hql = """
                update CurrencyJpa c 
                set c.name = :name,
                    c.code = :code,
                    c.symbol = :symbol
                where c.id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", event.getId())
                .set("name", event.getName())
                .set("code", event.getCode())
                .set("symbol", event.getSymbol())
                .update();
    }

    @Transactional
    @BusinessEventListener
    public void handlePropertyEvent(CurrencyPropertyEvent<?> event) {
        log.trace("[{}] - Processing currency property {} event", event.getCode(), event.getType());

        var updatePart = switch (event.getType()) {
            case ENABLED -> " c.enabled = :value";
            case DECIMAL_PLACES -> " c.decimalPlaces = :value";
        };

        var hql = "update CurrencyJpa c set"
                + updatePart
                + " where c.code = :code";

        entityManager.update()
                .hql(hql)
                .set("code", event.getCode())
                .set("value", event.getValue())
                .update();
    }

    protected Currency convert(CurrencyJpa source) {
        if (source == null) {
            return null;
        }

        return Currency.builder()
                .id(source.getId())
                .name(source.getName())
                .code(source.getCode())
                .symbol(source.getSymbol())
                .decimalPlaces(source.getDecimalPlaces())
                .enabled(source.isEnabled())
                .build();
    }

}
