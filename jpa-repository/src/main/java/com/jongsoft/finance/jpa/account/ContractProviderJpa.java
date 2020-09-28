package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.account.ContractProvider;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.account.entity.ContractJpa;
import com.jongsoft.finance.jpa.core.DataProviderJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.annotation.ReadOnly;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.sql.Connection;

@Slf4j
@ReadOnly
@Singleton
@Transactional
public class ContractProviderJpa extends DataProviderJpa<Contract, ContractJpa> implements ContractProvider {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;

    public ContractProviderJpa(
            AuthenticationFacade authenticationFacade,
            EntityManager entityManager,
            SynchronousTransactionManager<Connection> transactionManager) {
        super(entityManager, ContractJpa.class, transactionManager);
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<Contract> lookup() {
        log.trace("Contract listing");

        var hql = """
                select c from ContractJpa c
                where c.user.username = :username""";

        var query = entityManager.createQuery(hql)
                .setParameter("username", authenticationFacade.authenticated());

        return this.<ContractJpa>multiValue(query)
                .map(this::convert);
    }

    @Override
    public Maybe<Contract> lookup(String name) {
        log.trace("Contract lookup by name: {}", name);

        var hql = """
                select c from ContractJpa c
                where c.name = :name
                    and c.user.username = :username""";

        return maybe(hql,
                query -> query.setParameter("username", authenticationFacade.authenticated())
                        .setParameter("name", name));
    }

    @Override
    public Flowable<Contract> search(String partialName) {
        log.trace("Contract lookup by partial name: {}", partialName);

        var hql = """
                select c from ContractJpa c
                where lower(c.name) like lower(:name)
                    and c.user.username = :username""";

        return flow(hql,
                query -> query.setParameter("username", authenticationFacade.authenticated())
                        .setParameter("name", "%" + partialName + "%"));
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
