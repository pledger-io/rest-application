package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@ReadOnly
@Singleton
@Named("budgetProvider")
public class BudgetProviderJpa implements BudgetProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager reactiveEntityManager;

    public BudgetProviderJpa(AuthenticationFacade authenticationFacade, ReactiveEntityManager reactiveEntityManager) {
        this.authenticationFacade = authenticationFacade;
        this.reactiveEntityManager = reactiveEntityManager;
    }

    @Override
    public Sequence<Budget> lookup() {
        var hql = """
                select b from BudgetJpa b
                where b.user.username = :username
                order by b.from asc""";

        return reactiveEntityManager.<BudgetJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .sequence()
                .map(this::convert);
    }

    @Override
    public Optional<Budget> lookup(int year, int month) {
        var range = DateUtils.forMonth(year, month);

        var hql = """
                select b from BudgetJpa b
                where b.user.username = :username
                    and b.from <= :start
                    and (b.until is null or b.until >= :end)""";

        return reactiveEntityManager.<BudgetJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("start", range.from())
                .set("end", range.until())
                .maybe()
                .map(this::convert);
    }

    @Override
    public Optional<Budget> first() {
        var hql = """
                select b from BudgetJpa b
                where b.user.username = :username
                order by b.from asc""";

        return reactiveEntityManager.<BudgetJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .limit(1)
                .maybe()
                .map(this::convert);
    }

    private Budget convert(BudgetJpa source) {
        if (source == null) {
            return null;
        }

        return Budget.builder()
                .id(source.getId())
                .start(source.getFrom())
                .end(source.getUntil())
                .expectedIncome(source.getExpectedIncome())
                .expenses(Collections.List(source.getExpenses())
                        .map(e -> Budget.Expense.builder()
                                .lowerBound(e.getLowerBound().doubleValue())
                                .upperBound(e.getUpperBound().doubleValue())
                                .name(e.getExpense().getName())
                                .id(e.getExpense().getId())
                                .build()))
                .build();
    }
}
