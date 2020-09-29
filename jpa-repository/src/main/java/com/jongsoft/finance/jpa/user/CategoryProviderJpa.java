package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.core.DataProviderJpa;
import com.jongsoft.finance.jpa.user.entity.CategoryJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.SynchronousTransactionManager;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.sql.Connection;

@Singleton
@Named("categoryProvider")
public class CategoryProviderJpa extends DataProviderJpa<Category, CategoryJpa> implements CategoryProvider {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;

    public CategoryProviderJpa(
            AuthenticationFacade authenticationFacade,
            EntityManager entityManager,
            SynchronousTransactionManager<Connection> transactionManager) {
        super(entityManager, CategoryJpa.class);
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Category> lookup(String label) {
        var hql = """
                select c from CategoryJpa c
                where c.label = :label
                    and c.user.username = :username""";

        var query = entityManager.createQuery(hql);
        query.setParameter("label", label);
        query.setParameter("username", authenticationFacade.authenticated());

        return API.Option(convert(singleValue(query)));
    }

    @Override
    public ResultPage<Category> lookup(FilterCommand filterCommand) {
        if (filterCommand instanceof CategoryFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return queryPage(
                    delegate,
                    API.Option(delegate.pageSize() * delegate.page()),
                    API.Option(delegate.pageSize()));
        }

        throw new IllegalStateException("Cannot execute non JPA filter command on CategoryProviderJpa");
    }

    @Override
    public Sequence<Category> lookup() {
        var hql = """
                select c from CategoryJpa c
                where c.user.username = :username and c.archived = false""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());

        return this.<CategoryJpa>multiValue(query)
                .map(this::convert);
    }

    @Override
    protected Category convert(CategoryJpa source) {
        if (source == null) {
            return null;
        }

        return Category.builder()
                .id(source.getId())
                .description(source.getDescription())
                .label(source.getLabel())
                .lastActivity(source.getLastTransaction())
                .delete(source.isArchived())
                .user(UserAccount.builder()
                        .username(source.getUser().getUsername())
                        .build())
                .build();
    }

}
