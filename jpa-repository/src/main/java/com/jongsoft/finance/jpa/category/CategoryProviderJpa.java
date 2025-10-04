package com.jongsoft.finance.jpa.category;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ReadOnly
@Singleton
@RequiresJpa
@Named("categoryProvider")
public class CategoryProviderJpa implements CategoryProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    @Inject
    public CategoryProviderJpa(
            AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Category> lookup(long id) {
        logger.trace("Looking up category with id: {}", id);

        return entityManager
                .from(CategoryJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(this::convert);
    }

    @Override
    public Optional<Category> lookup(String label) {
        logger.trace("Looking up category with label: {}", label);

        return entityManager
                .from(CategoryJpa.class)
                .fieldEq("label", label)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(this::convert);
    }

    @Override
    public ResultPage<Category> lookup(FilterCommand filterCommand) {
        if (filterCommand instanceof CategoryFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.from(delegate).paged().map(this::convert);
        }

        throw new IllegalStateException(
                "Cannot execute non JPA filter command on CategoryProviderJpa");
    }

    @Override
    public Sequence<Category> lookup() {
        logger.trace("Looking up all categories.");

        return entityManager
                .from(CategoryJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .stream()
                .map(this::convert)
                .collect(ReactiveEntityManager.sequenceCollector());
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
                        .username(new UserIdentifier(source.getUser().getUsername()))
                        .build())
                .build();
    }
}
