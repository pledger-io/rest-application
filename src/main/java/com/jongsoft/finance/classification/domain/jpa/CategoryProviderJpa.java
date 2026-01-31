package com.jongsoft.finance.classification.domain.jpa;

import com.jongsoft.finance.banking.adapter.api.LinkableProvider;
import com.jongsoft.finance.classification.adapter.api.CategoryProvider;
import com.jongsoft.finance.classification.domain.jpa.entity.CategoryJpa;
import com.jongsoft.finance.classification.domain.jpa.filter.CategoryFilterCommand;
import com.jongsoft.finance.classification.domain.jpa.mapper.CategoryMapper;
import com.jongsoft.finance.classification.domain.model.Category;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ReadOnly
@Singleton
public class CategoryProviderJpa implements CategoryProvider, LinkableProvider<Category> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final CategoryMapper categoryMapper;

    @Inject
    public CategoryProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            CategoryMapper categoryMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public Optional<Category> lookup(long id) {
        logger.trace("Looking up category with id: {}", id);

        return entityManager
                .from(CategoryJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(categoryMapper::toDomain);
    }

    @Override
    public Optional<Category> lookup(String label) {
        logger.trace("Looking up category with label: {}", label);

        return entityManager
                .from(CategoryJpa.class)
                .fieldEq("label", label)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(categoryMapper::toDomain);
    }

    @Override
    public ResultPage<Category> lookup(FilterCommand filterCommand) {
        if (filterCommand instanceof CategoryFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.from(delegate).paged().map(categoryMapper::toDomain);
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
                .map(categoryMapper::toDomain)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public String typeOf() {
        return "CATEGORY";
    }
}
