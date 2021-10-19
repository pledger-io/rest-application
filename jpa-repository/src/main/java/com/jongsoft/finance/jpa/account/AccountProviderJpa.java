package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.SavingGoal;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.projections.TripleProjection;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.savings.SavingGoalJpa;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import com.jongsoft.lang.time.Range;
import io.micronaut.data.model.Sort;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@Named("accountProvider")
public class AccountProviderJpa implements AccountProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    public AccountProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Mono<Account> synonymOf(String synonym) {
        log.trace("Account synonym lookup with: {}", synonym);

        String hql = """
                select a.account from AccountSynonymJpa a
                where a.synonym = :synonym
                and a.account.user.username = :username
                and a.account.archived = false""";

        return entityManager.<AccountJpa>reactive()
                .hql(hql)
                .set("synonym", synonym)
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .map(this::convert);
    }

    @Override
    public Sequence<Account> lookup() {
        log.trace("Account listing");

        String hql = """
                select a 
                from AccountJpa a
                where a.user.username = :username 
                  and a.archived = false""";

        return entityManager.<AccountJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .sequence()
                .map(this::convert);
    }

    @Override
    public Optional<Account> lookup(long id) {
        return entityManager.<AccountJpa>blocking()
                .hql("from AccountJpa where id = :id and user.username = :username")
                .set("username", authenticationFacade.authenticated())
                .set("id", id)
                .maybe()
                .map(this::convert);
    }

    @Override
    public Mono<Account> lookup(String name) {
        log.trace("Account name lookup: {} for {}", name, authenticationFacade.authenticated());

        String hql = """
                select a
                from AccountJpa a
                where
                  a.name = :name
                  and a.user.username = :username
                  and a.archived = false""";

        return entityManager.<AccountJpa>reactive()
                .hql(hql)
                .set("name", name)
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .map(this::convert);
    }

    @Override
    public Mono<Account> lookup(SystemAccountTypes accountType) {
        log.trace("Account type lookup: {}", accountType);

        var hql = """
                select a 
                from AccountJpa a
                where 
                    a.type.label = :label
                    and a.user.username = :username
                    and a.archived = false""";

        return entityManager.<AccountJpa>reactive()
                .hql(hql)
                .set("label", accountType.label())
                .set("username", authenticationFacade.authenticated())
                .limit(1)
                .maybe()
                .map(this::convert);
    }

    @Override
    public ResultPage<Account> lookup(FilterCommand filter) {
        log.trace("Accounts by filter: {}", filter);

        if (filter instanceof AccountFilterCommand delegate) {
            var offset = delegate.page() * delegate.pageSize();
            delegate.user(authenticationFacade.authenticated());

            return entityManager.<AccountJpa>blocking()
                    .hql(delegate.generateHql())
                    .setAll(delegate.getParameters())
                    .limit(delegate.pageSize())
                    .offset(offset)
                    .sort(delegate.sort())
                    .page()
                    .map(this::convert);
        }

        throw new IllegalStateException("Cannot use non JPA filter on AccountProviderJpa");
    }

    @Override
    public Sequence<AccountSpending> top(FilterCommand filter, Range<LocalDate> range, boolean asc) {
        log.trace("Account top listing by filter: {}", filter);

        if (filter instanceof AccountFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            var hql = """
                    select new com.jongsoft.finance.jpa.projections.TripleProjection(
                                t.account, sum(t.amount), avg(t.amount))
                     from TransactionJpa t
                     where
                        t.journal.date >= :start and t.journal.date < :until
                        and t.deleted is null
                        and t.journal.user.username = :username
                        and t.account.id in (select distinct a.id %s)
                     group by t.account
                     having sum(t.amount) %s 0""".formatted(delegate.generateHql(), asc ? "<=" : ">=");

            return entityManager.<TripleProjection<AccountJpa, BigDecimal, Double>>blocking()
                    .hql(hql)
                    .setAll(delegate.getParameters())
                    .set("start", range.from())
                    .set("until", range.until())
                    .limit(delegate.pageSize())
                    .sort(Sort.of(asc ? Sort.Order.asc("sum(t.amount)") : Sort.Order.desc("sum(t.amount)")))
                    .sequence()
                    .map(projection -> new AccountSpendingImpl(
                            convert(projection.getFirst()),
                            projection.getSecond(),
                            projection.getThird()));
        }

        throw new IllegalStateException("Cannot use non JPA filter on AccountProviderJpa");
    }

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
                .imageFileToken(source.getImageFileToken())
                .firstTransaction(source.getFirstTransaction())
                .lastTransaction(source.getLastTransaction())
                .number(source.getNumber())
                .interest(source.getInterest())
                .interestPeriodicity(source.getInterestPeriodicity())
                .savingGoals(Collections.Set(this.convertSavingGoals(source.getSavingGoals())))
                .user(UserAccount.builder()
                        .id(source.getUser().getId())
                        .username(source.getUser().getUsername())
                        .build())
                .build();
    }

    private Set<SavingGoal> convertSavingGoals(Set<SavingGoalJpa> savingGoals) {
        if (savingGoals == null) {
            return Set.of();
        }

        return savingGoals.stream()
                .filter(Predicate.not(SavingGoalJpa::isArchived))
                .map(source -> SavingGoal.builder()
                        .id(source.getId())
                        .allocated(source.getAllocated())
                        .goal(source.getGoal())
                        .targetDate(source.getTargetDate())
                        .name(source.getName())
                        .description(source.getDescription())
                        .schedule(source.getPeriodicity() != null ? new ScheduleValue(source.getPeriodicity(), source.getInterval()) : null)
                        .account(Account.builder()
                                .id(source.getAccount().getId())
                                .name(source.getAccount().getName())
                                .build())
                        .build())
                .collect(Collectors.toSet());
    }
}
