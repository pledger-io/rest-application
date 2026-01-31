package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionJournal;
import com.jongsoft.finance.banking.domain.jpa.filter.TransactionFilterCommand;
import com.jongsoft.finance.banking.domain.jpa.mapper.TransactionMapper;
import com.jongsoft.finance.banking.domain.jpa.projection.DailySummaryImpl;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.core.domain.jpa.query.expression.Expressions;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

@ReadOnly
@Singleton
public class TransactionProviderJpa implements TransactionProvider {

    private final Logger log = LoggerFactory.getLogger(TransactionProviderJpa.class);

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    private final TransactionMapper transactionMapper;

    @Inject
    public TransactionProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            TransactionMapper transactionMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Optional<Transaction> first(FilterCommand filter) {
        log.trace("Transaction locate first with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.page(0, 1);
            delegate.user(authenticationFacade.authenticated());

            var results = entityManager
                    .from(delegate)
                    .join("transactions t")
                    .orderBy("date", true)
                    .paged()
                    .content()
                    .map(transactionMapper::toDomain);

            if (results.isEmpty()) {
                return Control.Option();
            }

            return Control.Option(results.head());
        }
        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Optional<Transaction> lookup(long id) {
        return entityManager
                .from(TransactionJournal.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("id", id)
                .singleResult()
                .map(transactionMapper::toDomain);
    }

    @Override
    public ResultPage<Transaction> lookup(FilterCommand filter) {
        log.trace("Transactions lookup with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager
                    .from(delegate)
                    .join("transactions t")
                    .paged()
                    .map(transactionMapper::toDomain);
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Sequence<DailySummary> daily(FilterCommand filter) {
        log.trace("Transactions daily sum with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager
                    .from(delegate)
                    .join("transactions t")
                    .groupBy("date")
                    .orderBy("date", true)
                    .project(DailySummaryImpl.class, "new DailySummaryImpl(e.date, sum(t.amount))")
                    .map(DailySummary.class::cast)
                    .collect(ReactiveEntityManager.sequenceCollector());
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Sequence<DailySummary> monthly(FilterCommand filter) {
        log.trace("Transactions monthly sum with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager
                    .from(delegate)
                    .join("transactions t")
                    // reset the order by statement, otherwise exceptions with the group by will
                    // happen
                    .orderBy(null, false)
                    .groupBy(Expressions.field("year(e.date)"), Expressions.field("month(e.date)"))
                    .project(
                            DailySummaryImpl.class,
                            "new DailySummaryImpl(year(e.date), month(e.date), 1, sum(t.amount))")
                    .sorted(Comparator.comparing(DailySummaryImpl::day))
                    .map(DailySummary.class::cast)
                    .collect(ReactiveEntityManager.sequenceCollector());
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Optional<BigDecimal> balance(FilterCommand filter) {
        log.trace("Transaction balance with filter: {}", filter.toString());

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager
                    .from(delegate)
                    .join("transactions t")
                    .orderBy(null, false)
                    .projectSingleValue(BigDecimal.class, "sum(t.amount)");
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Sequence<Transaction> similar(
            EntityRef from, EntityRef to, double amount, LocalDate date) {
        return entityManager
                .from(TransactionJournal.class)
                .joinFetch("transactions")
                .joinFetch("currency")
                .joinFetch("tags")
                .fieldEq("user.username", authenticationFacade.authenticated())
                .whereExists(fromQuery -> fromQuery
                        .from("transactions")
                        .fieldEq("account.id", from.getId())
                        .fieldEqOneOf("amount", amount, -amount)
                        .fieldNull("deleted"))
                .whereExists(toQuery -> toQuery.from("transactions")
                        .fieldEq("account.id", to.getId())
                        .fieldEqOneOf("amount", amount, -amount)
                        .fieldNull("deleted"))
                .stream()
                .map(transactionMapper::toDomain)
                .collect(com.jongsoft.lang.collection.support.Collections.collector(
                        com.jongsoft.lang.Collections::List));
    }
}
