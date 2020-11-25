package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.core.date.DateRangeOld;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.BudgetJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;
import io.reactivex.Maybe;
import io.reactivex.Single;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;

@Singleton
@Transactional
@Named("budgetProvider")
public class BudgetProviderJpa extends RepositoryJpa implements BudgetProvider {

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
    public Single<Budget> lookup(int year, int month) {
        var range = DateRangeOld.forMonth(year, month);

        var hql = """
                select b from BudgetJpa b
                where b.user.username = :username
                    and b.from <= :start
                    and (b.until is null or b.until > :end)""";

        return reactiveEntityManager.<BudgetJpa>reactive()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("start", range.getStart())
                .set("end", range.getEnd())
                .single()
                .map(this::convert);
    }

    @Override
    public Maybe<Budget> first() {
        var hql = """
                select b from BudgetJpa b
                where b.user.username = :username
                order by b.from asc""";

        return reactiveEntityManager.<BudgetJpa>reactive()
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
                                .lowerBound(e.getLowerBound())
                                .upperBound(e.getUpperBound())
                                .name(e.getExpense().getName())
                                .id(e.getExpense().getId())
                                .build()))
                .build();
    }
}
