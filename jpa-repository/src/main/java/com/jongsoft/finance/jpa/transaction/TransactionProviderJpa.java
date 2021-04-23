package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.jpa.category.CategoryJpa;
import com.jongsoft.finance.jpa.budget.ExpenseJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@Singleton
@Transactional
@Named("transactionProvider")
public class TransactionProviderJpa implements TransactionProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    public TransactionProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Maybe<Transaction> first(FilterCommand filter) {
        log.trace("Transaction locate first with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.<TransactionJournal>reactive()
                    .hql("select distinct a " + delegate.generateHql() + " order by a.date asc")
                    .setAll(delegate.getParameters())
                    .limit(1)
                    .maybe()
                    .map(this::convert);
        }
        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Optional<Transaction> lookup(long id) {
        return entityManager.<TransactionJournal>blocking()
                .hql("from TransactionJournal where id = :id and user.username = :username")
                .set("username", authenticationFacade.authenticated())
                .set("id", id)
                .maybe()
                .map(this::convert);
    }

    @Override
    public ResultPage<Transaction> lookup(FilterCommand filter) {
        log.trace("Transactions lookup with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            var offset = delegate.page() * delegate.pageSize();
            delegate.user(authenticationFacade.authenticated());

            return entityManager.<TransactionJournal>blocking()
                    .hql(delegate.generateHql())
                    .setAll(delegate.getParameters())
                    .limit(delegate.pageSize())
                    .offset(offset)
                    .sort(delegate.sort())
                    .page()
                    .map(this::convert);
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Sequence<DailySummary> daily(FilterCommand filter) {
        log.trace("Transactions daily sum with filter: {}", filter);

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            var hql = """
                    select new %s(
                       a.date,
                       sum(t.amount))
                       %s
                       group by a.date
                       order by a.date asc""".formatted(DailySummaryImpl.class.getName(), delegate.generateHql());

            return entityManager.<DailySummary>blocking()
                    .hql(hql)
                    .setAll(delegate.getParameters())
                    .sequence();
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionProviderJpa");
    }

    @Override
    public Optional<BigDecimal> balance(FilterCommand filter) {
        log.trace("Transaction balance with filter: {}", filter.toString());

        if (filter instanceof TransactionFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.<BigDecimal>blocking()
                    .hql("select sum(t.amount) " + delegate.generateHql())
                    .setAll(delegate.getParameters())
                    .maybe();
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

        return entityManager.<TransactionJournal>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("amount", amount)
                .set("date", date)
                .set("fromAccount", from.getId())
                .set("toAccount", to.getId())
                .sequence()
                .map(this::convert);
    }

    protected Transaction convert(TransactionJournal source) {
        if (source == null) {
            return null;
        }

        var parts = Collections.List(source.getTransactions())
                .filter(entity -> Objects.isNull(entity.getDeleted()))
                .map(this::convertPart);

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
                .budget(Control.Option(source.getBudget()).map(ExpenseJpa::getName).getOrSupply(() -> null))
                .category(Control.Option(source.getCategory()).map(CategoryJpa::getLabel).getOrSupply(() -> null))
                .currency(source.getCurrency().getCode())
                .importSlug(Control.Option(source.getBatchImport()).map(ImportJpa::getSlug).getOrSupply(() -> null))
                .description(source.getDescription())
                .contract(Control.Option(source.getContract()).map(ContractJpa::getName).getOrSupply(() -> null))
                .tags(Control.Option(source.getTags()).map(tags ->
                        Collections.List(tags).map(TagJpa::getName)).getOrSupply(Collections::List))
                .transactions(parts)
                .build();
    }

    private Transaction.Part convertPart(TransactionJpa transaction) {
        return Transaction.Part.builder()
                .id(transaction.getId())
                .account(
                        Account.builder()
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
