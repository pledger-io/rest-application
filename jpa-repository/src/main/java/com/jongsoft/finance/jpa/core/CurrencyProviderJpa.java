package com.jongsoft.finance.jpa.core;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.account.events.CurrencyCreatedEvent;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.core.events.CurrencyPropertyEvent;
import com.jongsoft.finance.domain.core.events.CurrencyRenameEvent;
import com.jongsoft.finance.jpa.core.entity.CurrencyJpa;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.sql.Connection;

@Slf4j
@Singleton
@Transactional
public class CurrencyProviderJpa extends DataProviderJpa<Currency, CurrencyJpa> implements CurrencyProvider {

    private final EntityManager entityManager;

    public CurrencyProviderJpa(
            EntityManager entityManager,
            SynchronousTransactionManager<Connection> transactionManager) {
        super(entityManager, CurrencyJpa.class, transactionManager);
        this.entityManager = entityManager;
    }

    @Override
    public Maybe<Currency> lookup(String code) {
        log.trace("Currency lookup by code: {}", code);

        var hql = """
                select c from CurrencyJpa c 
                where c.archived = false
                    and c.code = :code""";

        return maybe(
                hql,
                query -> query.setParameter("code", code));
    }

    @Override
    public Sequence<Currency> lookup() {
        log.trace("Currency listing");

        var hql = """
                select c from CurrencyJpa c 
                where c.archived = false""";

        var query = entityManager.createQuery(hql);

        return this.<CurrencyJpa>multiValue(query)
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

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getId());
        query.setParameter("name", event.getName());
        query.setParameter("code", event.getCode());
        query.setParameter("symbol", event.getSymbol());
        query.executeUpdate();
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

        var query = entityManager.createQuery(hql);
        query.setParameter("code", event.getCode());
        query.setParameter("value", event.getValue());
        query.executeUpdate();
    }

    @Override
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
