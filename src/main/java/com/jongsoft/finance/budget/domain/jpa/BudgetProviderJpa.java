package com.jongsoft.finance.budget.domain.jpa;

import static org.slf4j.LoggerFactory.getLogger;

import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.budget.domain.jpa.entity.BudgetJpa;
import com.jongsoft.finance.budget.domain.jpa.mapper.BudgetMapper;
import com.jongsoft.finance.budget.domain.model.Budget;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@ReadOnly
@Singleton
class BudgetProviderJpa implements BudgetProvider {

    private final Logger logger = getLogger(BudgetProviderJpa.class);

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager reactiveEntityManager;
    private final BudgetMapper budgetMapper;

    public BudgetProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager reactiveEntityManager,
            BudgetMapper budgetMapper) {
        this.authenticationFacade = authenticationFacade;
        this.reactiveEntityManager = reactiveEntityManager;
        this.budgetMapper = budgetMapper;
    }

    @Override
    public Sequence<Budget> lookup() {
        logger.trace("Fetching all budgets for user.");

        return reactiveEntityManager
                .from(BudgetJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .orderBy("from", true)
                .stream()
                .map(budgetMapper::toDomain)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public Optional<Budget> lookup(int year, int month) {
        logger.trace("Fetching budget for user in {}-{}.", year, month);
        var range = Dates.range(LocalDate.of(year, month, 1), ChronoUnit.MONTHS);

        return reactiveEntityManager
                .from(BudgetJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldLtOrEq("from", range.from())
                .fieldGtOrEqNullable("until", range.until())
                .singleResult()
                .map(budgetMapper::toDomain);
    }

    @Override
    public Optional<Budget> first() {
        logger.trace("Fetching first budget for user.");

        return reactiveEntityManager
                .from(BudgetJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .orderBy("from", true)
                .limit(1)
                .singleResult()
                .map(budgetMapper::toDomain);
    }
}
