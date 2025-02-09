package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.collection.support.Collections;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ReadOnly
@RequiresJpa
@Singleton
public class ContractProviderJpa implements ContractProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    @Inject
    public ContractProviderJpa(AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<Contract> lookup() {
        log.trace("Listing all contracts for user.");

        return entityManager.from(ContractJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .stream()
                .map(this::convert)
                .collect(Collections.collector(com.jongsoft.lang.Collections::List));
    }

    @Override
    public Optional<Contract> lookup(long id) {
        log.trace("Contract lookup by id {}.", id);

        return entityManager.from(ContractJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(this::convert);
    }

    @Override
    public Optional<Contract> lookup(String name) {
        log.trace("Contract lookup by name {}.", name);

        return entityManager.from(ContractJpa.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .singleResult()
                .map(this::convert);
    }

    @Override
    public Sequence<Contract> search(String partialName) {
        log.trace("Contract lookup by partial name '{}'.", partialName);

        return entityManager.from(ContractJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .fieldLike("name", partialName)
                .stream()
                .map(this::convert)
                .collect(Collections.collector(com.jongsoft.lang.Collections::List));
    }

    protected Contract convert(ContractJpa source) {
        if (source == null) {
            return null;
        }

        return Contract.builder()
                .id(source.getId())
                .name(source.getName())
                .uploaded(source.getFileToken() != null)
                .startDate(source.getStartDate())
                .endDate(source.getEndDate())
                .company(Account.builder()
                        .id(source.getCompany().getId())
                        .user(new UserIdentifier(source.getUser().getUsername()))
                        .name(source.getCompany().getName())
                        .type(source.getCompany().getType().getLabel())
                        .imageFileToken(source.getCompany().getImageFileToken())
                        .build())
                .notifyBeforeEnd(source.isWarningActive())
                .fileToken(source.getFileToken())
                .description(source.getDescription())
                .terminated(source.isArchived())
                .build();
    }
}
