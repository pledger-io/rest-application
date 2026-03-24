package com.jongsoft.finance.project.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.project.adapter.api.ClientProvider;
import com.jongsoft.finance.project.domain.jpa.entity.ClientJpa;
import com.jongsoft.finance.project.domain.jpa.mapper.ClientMapper;
import com.jongsoft.finance.project.domain.model.Client;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ReadOnly
@Singleton
class ClientProviderJpa implements ClientProvider {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final ClientMapper clientMapper;

    public ClientProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            ClientMapper clientMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.clientMapper = clientMapper;
    }

    @Override
    public Sequence<Client> lookup() {
        log.trace("Client listing");

        return entityManager
                .from(ClientJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .orderBy("name", true)
                .stream()
                .map(clientMapper::toDomain)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public Optional<Client> lookup(long id) {
        log.trace("Client lookup by id {}.", id);

        return entityManager
                .from(ClientJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .singleResult()
                .map(clientMapper::toDomain);
    }

    @Override
    public Optional<Client> lookup(String name) {
        log.trace("Client lookup by name {}.", name);

        return entityManager
                .from(ClientJpa.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .singleResult()
                .map(clientMapper::toDomain);
    }
}
