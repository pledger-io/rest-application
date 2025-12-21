package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.Classifier;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.query.expression.Expressions;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.providers.DataProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@ReadOnly
@Singleton
@RequiresJpa
@Named("transactionProvider")
public class TransactionProviderJpa implements TransactionProvider {

    private final Logger log = LoggerFactory.getLogger(TransactionProviderJpa.class);

    private final List<DataProvider<? extends Classifier>> metadataProviders;
    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    @Inject
    public TransactionProviderJpa(
            List<DataProvider<? extends Classifier>> metadataProviders,
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.metadataProviders = metadataProviders;
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
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
                    .map(this::convert);

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
                .map(this::convert);
    }

    @Override
    public ResultPage<Transaction> lookup(FilterCommand filter) {
        log.trace("Transactions lookup with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.from(delegate).join("transactions t").paged().map(this::convert);
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
                .map(this::convert)
                .collect(com.jongsoft.lang.collection.support.Collections.collector(
                        com.jongsoft.lang.Collections::List));
    }

    protected Transaction convert(TransactionJournal source) {
        if (source == null) {
            return null;
        }

        var builder = Transaction.builder()
                .id(source.getId())
                .created(source.getCreated())
                .updated(source.getUpdated())
                .date(source.getDate())
                .bookDate(source.getBookDate())
                .interestDate(source.getInterestDate())
                .failureCode(source.getFailureCode())
                .currency(source.getCurrency().getCode())
                .description(source.getDescription())
                .tags(Control.Option(source.getTags())
                        .map(tags -> Collections.List(tags).map(TagJpa::getName))
                        .getOrSupply(Collections::List))
                .deleted(source.getDeleted() != null);

        builder.transactions(source.getTransactions().stream()
                .map(this::convertPart)
                .collect(com.jongsoft.lang.collection.support.Collections.collector(
                        com.jongsoft.lang.Collections::List)));

        var metadata = new HashMap<String, Classifier>();
        for (var relation : source.getMetadata()) {
            metadataProviders.stream()
                    .filter(provider ->
                            Objects.equals(provider.typeOf(), relation.getRelationType()))
                    .map(provider -> provider.lookup(relation.getEntityId()))
                    .findFirst()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .ifPresent(matchingEntity ->
                            metadata.put(relation.getRelationType(), matchingEntity));
        }
        builder.metadata(metadata);

        return builder.build();
    }

    private Transaction.Part convertPart(TransactionJpa transaction) {
        return Transaction.Part.builder()
                .id(transaction.getId())
                .account(Account.builder()
                        .id(transaction.getAccount().getId())
                        .name(transaction.getAccount().getName())
                        .type(transaction.getAccount().getType().getLabel())
                        .imageFileToken(transaction.getAccount().getImageFileToken())
                        .build())
                .amount(transaction.getAmount().doubleValue())
                .description(transaction.getDescription())
                .build();
    }
}
