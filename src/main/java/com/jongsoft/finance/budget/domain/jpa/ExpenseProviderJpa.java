package com.jongsoft.finance.budget.domain.jpa;

import com.jongsoft.finance.banking.adapter.api.LinkableProvider;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.budget.adapter.api.ExpenseProvider;
import com.jongsoft.finance.budget.domain.jpa.entity.ExpenseJpa;
import com.jongsoft.finance.budget.domain.jpa.filter.ExpenseFilterCommand;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@ReadOnly
@Singleton
public class ExpenseProviderJpa
        implements ExpenseProvider, LinkableProvider<EntityRef.NamedEntity> {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    @Inject
    public ExpenseProviderJpa(
            AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<EntityRef.NamedEntity> lookup() {
        return entityManager
                .from(ExpenseJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .stream()
                .map(this::convert)
                .collect(ReactiveEntityManager.sequenceCollector());
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

    @Override
    public String typeOf() {
        return "EXPENSE";
    }

    protected EntityRef.NamedEntity convert(ExpenseJpa source) {
        if (source == null) {
            return null;
        }

        return new EntityRef.NamedEntity(source.getId(), source.getName());
    }
}
