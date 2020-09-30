package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.account.ContractProvider;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.account.entity.ContractJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@ReadOnly
@Singleton
@Transactional
public class ContractProviderJpa implements ContractProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    public ContractProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<Contract> lookup() {
        log.trace("Contract listing");

        var hql = """
                select c from ContractJpa c
                where c.user.username = :username""";

        return entityManager.<ContractJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .sequence()
                .map(this::convert);
    }

    @Override
    public Optional<Contract> lookup(long id) {
        return entityManager.<ContractJpa>blocking()
                .hql("from ContractJpa where id = :id")
                .set("id", id)
                .maybe()
                .map(this::convert);
    }

    @Override
    public Maybe<Contract> lookup(String name) {
        log.trace("Contract lookup by name: {}", name);

        var hql = """
                select c from ContractJpa c
                where c.name = :name
                    and c.user.username = :username""";

        return entityManager.<ContractJpa>reactive()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", name)
                .maybe()
                .map(this::convert);
    }

    @Override
    public Flowable<Contract> search(String partialName) {
        log.trace("Contract lookup by partial name: {}", partialName);

        var hql = """
                select c from ContractJpa c
                where lower(c.name) like lower(:name)
                    and c.user.username = :username""";

        return entityManager.<ContractJpa>reactive()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", "%" + partialName + "%")
                .flow()
                .map(this::convert);
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
