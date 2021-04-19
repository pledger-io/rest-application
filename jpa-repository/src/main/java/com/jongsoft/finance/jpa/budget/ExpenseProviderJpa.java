package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.control.Optional;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;

@Singleton
@Transactional
@Named("expenseProvider")
public class ExpenseProviderJpa implements ExpenseProvider {

    private AuthenticationFacade authenticationFacadea;
    private ReactiveEntityManager entityManager;

    public ExpenseProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.authenticationFacadea = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Budget.Expense> lookup(long id) {
        return entityManager.<ExpenseJpa>blocking()
                .hql("from ExpenseJpa where id = :id and user.username = :username")
                .set("id", id)
                .set("username", authenticationFacadea.authenticated())
                .maybe()
                .map(this::convert);
    }

    @Override
    public ResultPage<Budget.Expense> lookup(FilterCommand filter) {
        if (filter instanceof ExpenseFilterCommand delegate) {
            delegate.user(authenticationFacadea.authenticated());

            return entityManager.<ExpenseJpa>blocking()
                    .hql(delegate.generateHql())
                    .setAll(delegate.getParameters())
                    .limit(delegate.pageSize())
                    .offset(delegate.page() * delegate.pageSize())
                    .page()
                    .map(this::convert);
        }

        throw new IllegalStateException("Cannot use non JPA filter on ExpenseProviderJpa");
    }

    protected Budget.Expense convert(ExpenseJpa source) {
        if (source == null) {
            return null;
        }

        return Budget.Expense.builder()
                .name(source.getName())
                .id(source.getId())
                .build();
    }

}
