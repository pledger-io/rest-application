package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountSynonymJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionJpa;
import com.jongsoft.finance.banking.domain.jpa.filter.AccountFilterCommand;
import com.jongsoft.finance.banking.domain.jpa.mapper.AccountMapper;
import com.jongsoft.finance.banking.domain.jpa.projection.TripleProjection;
import com.jongsoft.finance.banking.domain.model.*;
import com.jongsoft.finance.banking.types.SystemAccountTypes;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import com.jongsoft.lang.time.Range;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

@ReadOnly
@Singleton
class AccountProviderJpa implements AccountProvider {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final AccountMapper accountMapper;

    public AccountProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            AccountMapper accountMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.accountMapper = accountMapper;
    }

    @Override
    public Optional<Account> synonymOf(String synonym) {
        log.trace("Account synonym lookup with {}.", synonym);

        return entityManager
                .from(AccountSynonymJpa.class)
                .fieldEq("synonym", synonym)
                .fieldEq("account.user.username", authenticationFacade.authenticated())
                .fieldEq("account.archived", false)
                .projectSingleValue(AccountJpa.class, "account")
                .map(accountMapper::toDomain);
    }

    @Override
    public Sequence<Account> lookup() {
        log.trace("Listing all accounts for user.");

        return entityManager
                .from(AccountJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .stream()
                .map(accountMapper::toDomain)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public Optional<Account> lookup(long id) {
        log.trace("Looking up account by id {}.", id);
        return entityManager
                .from(AccountJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> lookup(String name) {
        log.trace("Account name lookup: {} for {}", name, authenticationFacade.authenticated());

        return entityManager
                .from(AccountJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .fieldEq("name", name)
                .singleResult()
                .map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> lookup(SystemAccountTypes accountType) {
        log.trace("Account type lookup: {}", accountType);

        return entityManager
                .from(AccountJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .fieldEq("type.label", accountType.label())
                .singleResult()
                .map(accountMapper::toDomain);
    }

    @Override
    public ResultPage<Account> lookup(FilterCommand filter) {
        log.trace("Accounts by filter: {}", filter);

        if (filter instanceof AccountFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.from(delegate).paged().map(accountMapper::toDomain);
        }

        throw new IllegalStateException("Cannot use non JPA filter on AccountProviderJpa");
    }

    @Override
    public Sequence<AccountSpending> top(
            FilterCommand filter, Range<LocalDate> range, boolean asc) {
        log.trace("Account top listing by filter: {}", filter);

        if (filter instanceof AccountFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            var query = entityManager
                    .from(TransactionJpa.class)
                    .fieldIn("account.id", AccountJpa.class, subQuery -> {
                        delegate.applyTo(subQuery);
                        subQuery.project("id");
                    })
                    .fieldBetween("journal.date", range.from(), range.until())
                    .fieldEq("journal.user.username", authenticationFacade.authenticated())
                    .fieldNull("deleted")
                    .groupBy("account");

            // delegate.applyPagingOnly(query);
            return query.project(
                            TripleProjection.class,
                            "new com.jongsoft.finance.banking.domain.jpa.projection.TripleProjection(e.account,"
                                    + " sum(e.amount), avg(e.amount))")
                    .map(triplet -> (TripleProjection<AccountJpa, BigDecimal, Double>) triplet)
                    .map(projection -> (AccountSpending) new AccountSpendingImpl(
                            accountMapper.toDomain(projection.getFirst()),
                            projection.getSecond(),
                            projection.getThird()))
                    .collect(ReactiveEntityManager.sequenceCollector());
        }

        throw new IllegalStateException("Cannot use non JPA filter on AccountProviderJpa");
    }
}
