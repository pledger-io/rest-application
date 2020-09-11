package com.jongsoft.finance.jpa.transaction;

import java.time.LocalDate;
import java.util.Objects;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.account.entity.ContractJpa;
import com.jongsoft.finance.jpa.core.DataProviderJpa;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.transaction.entity.TagJpa;
import com.jongsoft.finance.jpa.transaction.entity.TransactionJournal;
import com.jongsoft.finance.jpa.transaction.entity.TransactionJpa;
import com.jongsoft.finance.jpa.user.entity.CategoryJpa;
import com.jongsoft.finance.jpa.user.entity.ExpenseJpa;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
@Named("transactionProvider")
public class TransactionProviderJpa extends DataProviderJpa<Transaction, TransactionJournal> implements TransactionProvider {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;

    public TransactionProviderJpa(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        super(entityManager, TransactionJournal.class);
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Transaction> first(FilterCommand filter) {
        log.trace("Transaction locate first with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            var hql = "select distinct a " + delegate.generateHql() + " order by a.date asc";
            var query = entityManager.createQuery(hql);
            delegate.prepareQuery(query);
            query.setMaxResults(1);

            return API.Option(convert(this.<TransactionJournal>singleValue(query)));
        }
        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public ResultPage<Transaction> lookup(FilterCommand filter) {
        log.trace("Transactions lookup with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            var offset = delegate.page() * delegate.pageSize();
            delegate.user(authenticationFacade.authenticated());

            return queryPage(
                    delegate,
                    API.Option(offset),
                    API.Option(delegate.pageSize()));
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Sequence<DailySummary> daily(FilterCommand filter) {
        log.trace("Transactions daily sum with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            var hql = """
                    select new com.jongsoft.finance.jpa.transaction.entity.DailySummaryImpl(
                       a.date,
                       sum(t.amount))
                       %s
                       group by a.date""".formatted(delegate.generateHql());

            var query = entityManager.createQuery(hql);
            delegate.prepareQuery(query);

            return this.multiValue(query);
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Optional<Double> balance(FilterCommand filter) {
        log.trace("Transaction balance with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            var hql = "select sum(t.amount) " + delegate.generateHql();
            var query = entityManager.createQuery(hql);
            delegate.prepareQuery(query);

            return API.Option(this.<Double>singleValue(query));
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Sequence<Transaction> similar(EntityRef from, EntityRef to, double amount, LocalDate date) {
        var hql = """
                select distinct t from TransactionJournal t 
                where t.user.username = :username 
                    and t.date = :date
                    and exists (
                        select 1 from t.transactions tj 
                        where abs(tj.amount) = abs(:amount) 
                            and tj.account.id = :fromAccount
                            and tj.deleted is null)
                    and exists (
                        select 1 from t.transactions tj
                        where abs(tj.amount) = abs(:amount)
                            and tj.account.id = :toAccount 
                            and tj.deleted is null)""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("amount", amount);
        query.setParameter("date", date);
        query.setParameter("fromAccount", from.getId());
        query.setParameter("toAccount", to.getId());

        return this.<TransactionJournal>multiValue(query)
                .map(this::convert);
    }

    @Override
    protected Transaction convert(TransactionJournal source) {
        if (source == null) {
            return null;
        }

        var parts = API.List(source.getTransactions())
                .filter(entity -> Objects.isNull(entity.getDeleted()))
                .map(this::convert);

        return Transaction.builder()
                .id(source.getId())
                .user(
                        UserAccount.builder()
                                .username(source.getUser().getUsername())
                                .build())
                .created(source.getCreated())
                .updated(source.getUpdated())
                .date(source.getDate())
                .bookDate(source.getBookDate())
                .interestDate(source.getInterestDate())
                .failureCode(source.getFailureCode())
                .budget(API.Option(source.getBudget()).map(ExpenseJpa::getName).getOrSupply(() -> null))
                .category(API.Option(source.getCategory()).map(CategoryJpa::getLabel).getOrSupply(() -> null))
                .currency(source.getCurrency().getCode())
                .importSlug(API.Option(source.getBatchImport()).map(ImportJpa::getSlug).getOrSupply(() -> null))
                .description(source.getDescription())
                .contract(API.Option(source.getContract()).map(ContractJpa::getName).getOrSupply(() -> null))
                .tags(API.Option(source.getTags()).map(tags ->
                        API.List(tags).map(TagJpa::getName)).getOrSupply(API::List))
                .transactions(parts)
                .build();
    }

    private Transaction.Part convert(TransactionJpa transaction) {
        return Transaction.Part.builder()
                .id(transaction.getId())
                .account(
                        Account.builder()
                                .id(transaction.getAccount().getId())
                                .name(transaction.getAccount().getName())
                                .type(transaction.getAccount().getType().getLabel())
                                .build())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .build();
    }
}
