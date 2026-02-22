package com.jongsoft.finance.core.domain.jpa;

import com.jongsoft.finance.core.adapter.api.CurrencyProvider;
import com.jongsoft.finance.core.domain.jpa.entity.CurrencyJpa;
import com.jongsoft.finance.core.domain.jpa.mapper.CurrencyMapper;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.core.domain.model.Currency;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.collection.support.Collections;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;

@ReadOnly
@Singleton
public class CurrencyProviderJpa implements CurrencyProvider {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(CurrencyProviderJpa.class);

    private final ReactiveEntityManager entityManager;
    private final CurrencyMapper currencyMapper;

    @Inject
    public CurrencyProviderJpa(ReactiveEntityManager entityManager, CurrencyMapper currencyMapper) {
        this.entityManager = entityManager;
        this.currencyMapper = currencyMapper;
    }

    public Optional<Currency> lookup(long id) {
        log.trace("Currency lookup by id {}.", id);

        return entityManager
                .from(CurrencyJpa.class)
                .fieldEq("id", id)
                .singleResult()
                .map(currencyMapper::toDomain);
    }

    @Override
    public Optional<Currency> lookup(String code) {
        log.trace("Currency lookup by code {}.", code);

        return entityManager
                .from(CurrencyJpa.class)
                .fieldEq("code", code)
                .fieldEq("archived", false)
                .singleResult()
                .map(currencyMapper::toDomain);
    }

    @Override
    public Sequence<Currency> lookup() {
        log.trace("Listing all currencies in the system.");

        return entityManager.from(CurrencyJpa.class).fieldEq("archived", false).stream()
                .map(currencyMapper::toDomain)
                .collect(Collections.collector(com.jongsoft.lang.Collections::List));
    }
}
