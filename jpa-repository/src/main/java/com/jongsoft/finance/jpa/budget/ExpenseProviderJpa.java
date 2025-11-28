package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@ReadOnly
@Singleton
@Named("expenseProvider")
public class ExpenseProviderJpa implements ExpenseProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    @Inject
    public ExpenseProviderJpa(
            AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<EntityRef.NamedEntity> lookup(long id) {
        return entityManager
                .from(ExpenseJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(this::convert);
    }

    @Override
    public ResultPage<EntityRef.NamedEntity> lookup(FilterCommand filter) {
        if (filter instanceof ExpenseFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.from(delegate).paged().map(this::convert);
        }

        throw new IllegalStateException("Cannot use non JPA filter on ExpenseProviderJpa");
    }

    protected EntityRef.NamedEntity convert(ExpenseJpa source) {
        if (source == null) {
            return null;
        }

        return new EntityRef.NamedEntity(source.getId(), source.getName());
    }
}
