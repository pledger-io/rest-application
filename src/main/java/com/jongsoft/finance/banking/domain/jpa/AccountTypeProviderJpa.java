package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.banking.adapter.api.AccountTypeProvider;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountTypeJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.lang.collection.Sequence;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

@ReadOnly
@Singleton
class AccountTypeProviderJpa implements AccountTypeProvider {

    public static final Logger LOGGER = LoggerFactory.getLogger(AccountTypeProviderJpa.class);

    private final ReactiveEntityManager entityManager;

    AccountTypeProviderJpa(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<String> lookup(boolean hidden) {
        LOGGER.debug("Locating account types with hidden: {}", hidden);

        return entityManager.from(AccountTypeJpa.class).fieldEq("hidden", hidden).stream()
                .sorted(Comparator.comparing(AccountTypeJpa::getLabel))
                .map(AccountTypeJpa::getLabel)
                .collect(ReactiveEntityManager.sequenceCollector());
    }
}
