package com.jongsoft.finance.jpa.currency;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.providers.CurrencyProvider;
import com.jongsoft.lang.collection.Sequence;
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
        return entityManager.<CurrencyJpa>blocking()
                .hql("from CurrencyJpa where id = :id")
                .set("id", id)
                .maybe()
                .map(this::convert);
    }

    @Override
    public Optional<Currency> lookup(String code) {
        log.trace("Currency lookup by code: {}", code);

        var hql = """
                select c from CurrencyJpa c
                where c.archived = false
                    and c.code = :code""";

        return entityManager.<CurrencyJpa>blocking()
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
