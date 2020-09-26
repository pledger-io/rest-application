package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.ExpenseProvider;
import com.jongsoft.finance.jpa.core.DataProviderJpa;
import com.jongsoft.finance.jpa.user.entity.ExpenseJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.API;
import io.micronaut.transaction.SynchronousTransactionManager;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.sql.Connection;

@Singleton
@Named("expenseProvider")
public class ExpenseProviderJpa extends DataProviderJpa<Budget.Expense, ExpenseJpa> implements ExpenseProvider {

    private AuthenticationFacade authenticationFacadea;
    private EntityManager entityManager;

    public ExpenseProviderJpa(
            AuthenticationFacade authenticationFacade,
            EntityManager entityManager,
            SynchronousTransactionManager<Connection> transactionManager) {
        super(entityManager, ExpenseJpa.class, transactionManager);
        this.authenticationFacadea = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public ResultPage<Budget.Expense> lookup(FilterCommand filter) {
        if (filter instanceof ExpenseFilterCommand delegate) {
            delegate.user(authenticationFacadea.authenticated());

            return queryPage(
                    delegate,
                    API.Option(),
                    API.Option());
        }

        throw new IllegalStateException("Cannot use non JPA filter on ExpenseProviderJpa");
    }

    @Override
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
