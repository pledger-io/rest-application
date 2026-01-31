package com.jongsoft.finance.contract.domain.jpa;

import com.jongsoft.finance.contract.adapter.api.ContractProvider;
import com.jongsoft.finance.contract.domain.jpa.entity.ContractJpa;
import com.jongsoft.finance.contract.domain.jpa.mapper.ContractMapper;
import com.jongsoft.finance.contract.domain.model.Contract;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.collection.support.Collections;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@ReadOnly
@Singleton
class ContractProviderJpa implements ContractProvider {
    private final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ContractProviderJpa.class);

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    private final ContractMapper contractMapper;

    @Inject
    public ContractProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            ContractMapper contractMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.contractMapper = contractMapper;
    }

    @Override
    public Sequence<Contract> lookup() {
        log.trace("Listing all contracts for user.");

        return entityManager
                .from(ContractJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .stream()
                .map(contractMapper::mapToDomain)
                .collect(Collections.collector(com.jongsoft.lang.Collections::List));
    }

    @Override
    public Optional<Contract> lookup(long id) {
        log.trace("Contract lookup by id {}.", id);

        return entityManager
                .from(ContractJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(contractMapper::mapToDomain);
    }

    @Override
    public Optional<Contract> lookup(String name) {
        log.trace("Contract lookup by name {}.", name);

        return entityManager
                .from(ContractJpa.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .singleResult()
                .map(contractMapper::mapToDomain);
    }

    @Override
    public Sequence<Contract> search(String partialName) {
        log.trace("Contract lookup by partial name '{}'.", partialName);

        return entityManager
                .from(ContractJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .fieldLike("name", partialName)
                .stream()
                .map(contractMapper::mapToDomain)
                .collect(Collections.collector(com.jongsoft.lang.Collections::List));
    }
}
