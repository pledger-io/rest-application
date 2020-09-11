package com.jongsoft.finance.jpa.user;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.user.entity.BudgetJpa;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

@Singleton
@Named("budgetProvider")
public class BudgetProviderJpa extends RepositoryJpa implements BudgetProvider {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;

    public BudgetProviderJpa(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<Budget> lookup() {
        var hql = """
                select b from BudgetJpa b
                where b.user.username = :username
                order by b.from asc""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());

        return this.<BudgetJpa>multiValue(query)
                .map(this::convert);
    }

    @Override
    public Optional<Budget> lookup(int year, int month) {
        var range = DateRange.forMonth(year, month);

        var hql = """
                select b from BudgetJpa b
                where b.user.username = :username
                    and b.from <= :start
                    and (b.until is null or b.until > :end)""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("start", range.getStart());
        query.setParameter("end", range.getEnd());

        return API.Option(convert(singleValue(query)));
    }

    @Override
    public Optional<Budget> first() {
        var hql = """
                select b from BudgetJpa b
                where b.user.username = :username
                order by b.from asc""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setMaxResults(1);

        return API.Option(convert(singleValue(query)));
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
                .expenses(API.List(source.getExpenses())
                        .map(e -> Budget.Expense.builder()
                                .lowerBound(e.getLowerBound())
                                .upperBound(e.getUpperBound())
                                .name(e.getExpense().getName())
                                .id(e.getExpense().getId())
                                .build()))
                .build();
    }
}
