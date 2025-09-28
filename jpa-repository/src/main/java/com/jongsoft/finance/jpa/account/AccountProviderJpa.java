package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.SavingGoal;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.projections.TripleProjection;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.savings.SavingGoalJpa;
import com.jongsoft.finance.jpa.transaction.TransactionJpa;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import com.jongsoft.lang.time.Range;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ReadOnly
@Singleton
@RequiresJpa
@Named("accountProvider")
public class AccountProviderJpa implements AccountProvider {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final AuthenticationFacade authenticationFacade;
  private final ReactiveEntityManager entityManager;

  public AccountProviderJpa(
      AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
    this.authenticationFacade = authenticationFacade;
    this.entityManager = entityManager;
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
        .map(this::convert);
  }

  @Override
  public Sequence<Account> lookup() {
    log.trace("Listing all accounts for user.");

    return entityManager
        .from(AccountJpa.class)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .fieldEq("archived", false)
        .stream()
        .map(this::convert)
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
        .map(this::convert);
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
        .map(this::convert);
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
        .map(this::convert);
  }

  @Override
  public ResultPage<Account> lookup(FilterCommand filter) {
    log.trace("Accounts by filter: {}", filter);

    if (filter instanceof AccountFilterCommand delegate) {
      delegate.user(authenticationFacade.authenticated());

      return entityManager.from(delegate).paged().map(this::convert);
    }

    throw new IllegalStateException("Cannot use non JPA filter on AccountProviderJpa");
  }

  @Override
  public Sequence<AccountSpending> top(FilterCommand filter, Range<LocalDate> range, boolean asc) {
    log.trace("Account top listing by filter: {}", filter);

    if (filter instanceof AccountFilterCommand delegate) {
      delegate.user(authenticationFacade.authenticated());

      var query =
          entityManager
              .from(TransactionJpa.class)
              .fieldIn(
                  "account.id",
                  AccountJpa.class,
                  subQuery -> {
                    delegate.applyTo(subQuery);
                    subQuery.project("id");
                  })
              .fieldBetween("journal.date", range.from(), range.until())
              .fieldEq("journal.user.username", authenticationFacade.authenticated())
              .fieldNull("deleted")
              .groupBy("account");

      // delegate.applyPagingOnly(query);
      return query
          .project(
              TripleProjection.class,
              "new com.jongsoft.finance.jpa.projections.TripleProjection(e.account,"
                  + " sum(e.amount), avg(e.amount))")
          .map(triplet -> (TripleProjection<AccountJpa, BigDecimal, Double>) triplet)
          .map(
              projection ->
                  (AccountSpending)
                      new AccountSpendingImpl(
                          convert(projection.getFirst()),
                          projection.getSecond(),
                          projection.getThird()))
          .collect(ReactiveEntityManager.sequenceCollector());
    }

    throw new IllegalStateException("Cannot use non JPA filter on AccountProviderJpa");
  }

  protected Account convert(AccountJpa source) {
    if (source == null
        || !Objects.equals(authenticationFacade.authenticated(), source.getUser().getUsername())) {
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
        .user(new UserIdentifier(source.getUser().getUsername()))
        .build();
  }

  private Set<SavingGoal> convertSavingGoals(Set<SavingGoalJpa> savingGoals) {
    if (savingGoals == null) {
      return Set.of();
    }

    return savingGoals.stream()
        .filter(Predicate.not(SavingGoalJpa::isArchived))
        .map(
            source ->
                SavingGoal.builder()
                    .id(source.getId())
                    .allocated(source.getAllocated())
                    .goal(source.getGoal())
                    .targetDate(source.getTargetDate())
                    .name(source.getName())
                    .description(source.getDescription())
                    .schedule(
                        source.getPeriodicity() != null
                            ? new ScheduleValue(source.getPeriodicity(), source.getInterval())
                            : null)
                    .account(
                        Account.builder()
                            .id(source.getAccount().getId())
                            .name(source.getAccount().getName())
                            .build())
                    .build())
        .collect(Collectors.toSet());
  }
}
