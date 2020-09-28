package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.account.entity.AccountJpa;
import com.jongsoft.finance.jpa.core.DataProviderJpa;
import com.jongsoft.finance.jpa.projections.TripleProjection;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.SynchronousTransactionManager;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.Objects;

@Slf4j
@Singleton
@Transactional
@Named("accountProvider")
public class AccountProviderJpa extends DataProviderJpa<Account, AccountJpa> implements AccountProvider {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;

    public AccountProviderJpa(
            AuthenticationFacade authenticationFacade,
            EntityManager entityManager,
            SynchronousTransactionManager<Connection> transactionManager) {
        super(entityManager, AccountJpa.class, transactionManager);
        this.authenticationFacade = authenticationFacade;

        this.entityManager = entityManager;
    }

    @Override
    public Optional<Account> synonymOf(String synonym) {
        log.trace("Account synonym lookup with: {}", synonym);

        String hql = """
                select a.account from AccountSynonymJpa a
                where a.synonym = :synonym
                and a.account.user.username = :username
                and a.account.archived = false""";

        var query = entityManager.createQuery(hql);
        query.setParameter("synonym", synonym);
        query.setParameter("username", authenticationFacade.authenticated());
        return API.Option(convert(singleValue(query)));
    }

    @Override
    public Sequence<Account> lookup() {
        log.trace("Account listing");

        String hql = """
                select a 
                from AccountJpa a
                where a.user.username = :username 
                  and a.archived = false""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());

        return this.<AccountJpa>multiValue(query)
                .map(this::convert);
    }

    @Override
    public Optional<Account> lookup(String name) {
        log.trace("Account name lookup: {}", name);

        String hql = """
                select a 
                from AccountJpa a
                where 
                  a.name = :name
                  and a.user.username = :username
                  and a.archived = false""";

        var query = entityManager.createQuery(hql);
        query.setParameter("name", name);
        query.setParameter("username", authenticationFacade.authenticated());

        return API.Option(convert(singleValue(query)));
    }

    @Override
    public Optional<Account> lookup(SystemAccountTypes accountType) {
        log.trace("Account type lookup: {}", accountType);

        var hql = """
                select a 
                from AccountJpa a
                where 
                    a.type.label = :label
                    and a.user.username = :username
                    and a.archived = false""";
        var query = entityManager.createQuery(hql);

        query.setParameter("label", accountType.label());
        query.setParameter("username", authenticationFacade.authenticated());
        query.setMaxResults(1);

        return API.Option(convert(singleValue(query)));
    }

    @Override
    public ResultPage<Account> lookup(FilterCommand filter) {
        log.trace("Accounts by filter: {}", filter);

        if (filter instanceof AccountFilterCommand delegate) {
            var offset = delegate.page() * delegate.pageSize();

            delegate.user(authenticationFacade.authenticated());
            return queryPage(
                    delegate,
                    API.Option(offset),
                    API.Option(delegate.pageSize()));
        }

        throw new IllegalStateException("Cannot use non JPA filter on AccountProviderJpa");
    }

    @Override
    public Sequence<AccountSpending> top(FilterCommand filter, DateRange range) {
        log.trace("Account top listing by filter: {}", filter);

        if (filter instanceof AccountFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            var hql = """
                    select new com.jongsoft.finance.jpa.projections.TripleProjection(
                                t.account, sum(t.amount), avg(t.amount))
                     from TransactionJpa t 
                     where
                        t.journal.date between :start and :until
                        and t.deleted is null
                        and t.journal.user.username = :username
                        and t.account.id in (select distinct a.id %s)
                     group by t.account
                     order by sum(t.amount) DESC""".formatted(delegate.generateHql());

            var query = entityManager.createQuery(hql);
            delegate.prepareQuery(query);
            query.setParameter("start", range.getStart());
            query.setParameter("until", range.getEnd());
            query.setMaxResults(delegate.pageSize());

            Sequence<TripleProjection<AccountJpa, Double, Double>> spendings = multiValue(query);
            return spendings.map(projection -> new AccountSpendingImpl(
                    convert(projection.getFirst()),
                    projection.getSecond(),
                    projection.getThird()));
        }

        throw new IllegalStateException("Cannot use non JPA filter on AccountProviderJpa");
    }

    @Override
    protected Account convert(AccountJpa source) {
        if (source == null || !Objects.equals(authenticationFacade.authenticated(), source.getUser().getUsername())) {
            return null;
        }

        return Account.builder()
                .id(source.getId())
                .name(source.getName())
                .description(source.getDescription())
                .type(source.getType().getLabel())
                .currency(source.getCurrency().getCode())
                .iban(source.getIban())
                .bic(source.getBic())
                .balance(java.util.Optional.ofNullable(source.getBalance()).orElse(0D))
                .firstTransaction(source.getFirstTransaction())
                .lastTransaction(source.getLastTransaction())
                .number(source.getNumber())
                .interest(source.getInterest())
                .interestPeriodicity(source.getInterestPeriodicity())
                .user(UserAccount.builder()
                        .id(source.getUser().getId())
                        .username(source.getUser().getUsername())
                        .build())
                .build();
    }
}