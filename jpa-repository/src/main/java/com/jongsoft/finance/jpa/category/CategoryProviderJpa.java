package com.jongsoft.finance.jpa.category;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Maybe;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;

@Singleton
@Named("categoryProvider")
public class CategoryProviderJpa implements CategoryProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    public CategoryProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Category> lookup(long id) {
        return entityManager.<CategoryJpa>blocking()
                .hql("from CategoryJpa where id = :id and user.username = :username")
                .set("id", id)
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .map(this::convert);
    }

    @Override
    public Maybe<Category> lookup(String label) {
        var hql = """
                select c from CategoryJpa c
                where c.label = :label
                    and c.user.username = :username""";

        return entityManager.<CategoryJpa>reactive()
                .hql(hql)
                .set("label", label)
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .map(this::convert);
    }

    @Override
    public ResultPage<Category> lookup(FilterCommand filterCommand) {
        if (filterCommand instanceof CategoryFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.<CategoryJpa>blocking()
                    .hql(delegate.generateHql())
                    .setAll(delegate.getParameters())
                    .limit(((CategoryFilterCommand) filterCommand).pageSize())
                    .offset(delegate.pageSize() * delegate.page())
                    .page()
                    .map(this::convert);
        }

        throw new IllegalStateException("Cannot execute non JPA filter command on CategoryProviderJpa");
    }

    @Override
    public Sequence<Category> lookup() {
        var hql = """
                select c from CategoryJpa c
                where c.user.username = :username and c.archived = false""";

        return entityManager.<CategoryJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .sequence()
                .map(this::convert);
    }

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
