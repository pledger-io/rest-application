package com.jongsoft.finance.invoice.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.invoice.adapter.api.TaxBracketProvider;
import com.jongsoft.finance.invoice.domain.jpa.entity.TaxBracketJpa;
import com.jongsoft.finance.invoice.domain.jpa.mapper.TaxBracketMapper;
import com.jongsoft.finance.invoice.domain.model.TaxBracket;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ReadOnly
@Singleton
class TaxBracketProviderJpa implements TaxBracketProvider {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final TaxBracketMapper taxBracketMapper;

    public TaxBracketProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            TaxBracketMapper taxBracketMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.taxBracketMapper = taxBracketMapper;
    }

    @Override
    public Optional<TaxBracket> lookup(long id) {
        log.trace("TaxBracket lookup by id {}.", id);

        return entityManager
                .from(TaxBracketJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(taxBracketMapper::toDomain);
    }

    @Override
    public Optional<TaxBracket> lookup(String name) {
        log.trace("TaxBracket lookup by name {}.", name);

        return entityManager
                .from(TaxBracketJpa.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(taxBracketMapper::toDomain);
    }
}
