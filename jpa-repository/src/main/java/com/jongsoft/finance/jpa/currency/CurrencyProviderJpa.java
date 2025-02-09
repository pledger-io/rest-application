package com.jongsoft.finance.jpa.currency;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.CurrencyProvider;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.collection.support.Collections;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ReadOnly
@Singleton
@RequiresJpa
public class CurrencyProviderJpa implements CurrencyProvider {

    private final ReactiveEntityManager entityManager;

    @Inject
    public CurrencyProviderJpa(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Currency> lookup(long id) {
        log.trace("Currency lookup by id {}.", id);

        return entityManager.from(CurrencyJpa.class)
                .fieldEq("id", id)
                .singleResult()
                .map(this::convert);
    }

    @Override
    public Optional<Currency> lookup(String code) {
        log.trace("Currency lookup by code {}.", code);

        return entityManager.from(CurrencyJpa.class)
                .fieldEq("code", code)
                .fieldEq("archived", false)
                .singleResult()
                .map(this::convert);
    }

    @Override
    public Sequence<Currency> lookup() {
        log.trace("Listing all currencies in the system.");

        return entityManager.from(CurrencyJpa.class)
                .fieldEq("archived", false)
                .stream()
                .map(this::convert)
                .collect(Collections.collector(com.jongsoft.lang.Collections::List));
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
