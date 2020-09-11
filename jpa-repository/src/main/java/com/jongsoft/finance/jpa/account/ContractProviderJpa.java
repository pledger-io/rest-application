package com.jongsoft.finance.jpa.account;

import javax.inject.Singleton;
import javax.persistence.EntityManager;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.account.ContractProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.account.entity.ContractJpa;
import com.jongsoft.finance.jpa.core.DataProviderJpa;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ContractProviderJpa extends DataProviderJpa<Contract, ContractJpa> implements ContractProvider {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;

    public ContractProviderJpa(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        super(entityManager, ContractJpa.class);
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<Contract> lookup() {
        log.trace("Contract listing");

        var hql = """
                select c from ContractJpa c
                where c.user.username = :username""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());

        return this.<ContractJpa>multiValue(query)
                .map(this::convert);
    }

    @Override
    public Optional<Contract> lookup(String name) {
        log.trace("Contract lookup by name: {}", name);

        var hql = """
                select c from ContractJpa c
                where c.name = :name
                    and c.user.username = :username""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("name", name);
        return API.Option(convert(singleValue(query)));
    }

    @Override
    public Sequence<Contract> search(String partialName) {
        log.trace("Contract lookup by partial name: {}", partialName);

        var hql = """
                select c from ContractJpa c
                where lower(c.name) like lower(:name)
                    and c.user.username = :username""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("name", "%" + partialName + "%");

        return this.<ContractJpa>multiValue(query)
                .map(this::convert);
    }

    @Override
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
                        .user(UserAccount.builder()
                                .username(source.getUser().getUsername())
                                .build())
                        .name(source.getCompany().getName())
                        .type(source.getCompany().getType().getLabel())
                        .build())
                .notifyBeforeEnd(source.isWarningActive())
                .fileToken(source.getFileToken())
                .description(source.getDescription())
                .terminated(source.isArchived())
                .build();
    }
}
