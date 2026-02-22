package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.banking.adapter.api.TagProvider;
import com.jongsoft.finance.banking.domain.jpa.entity.TagJpa;
import com.jongsoft.finance.banking.domain.jpa.filter.TagFilterCommand;
import com.jongsoft.finance.banking.domain.model.Tag;
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
public class TagProviderJpa implements TagProvider {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagProviderJpa.class);

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    @Inject
    public TagProviderJpa(
            AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<Tag> lookup() {
        log.trace("Tag listing");

        return entityManager
                .from(TagJpa.class)
                .joinFetch("user")
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .stream()
                .map(this::convert)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public Optional<Tag> lookup(String name) {
        log.trace("Tag lookup by name: {}", name);

        return entityManager
                .from(TagJpa.class)
                .joinFetch("user")
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("name", name)
                .fieldEq("archived", false)
                .singleResult()
                .map(this::convert);
    }

    @Override
    public ResultPage<Tag> lookup(FilterCommand filter) {
        log.trace("Tag lookup by filter: {}", filter);

        if (filter instanceof TagFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.from(delegate).paged().map(this::convert);
        }

        throw new IllegalStateException("Cannot use non JPA filter on TagProviderJpa");
    }

    protected Tag convert(TagJpa source) {
        if (source == null) {
            return null;
        }

        return new Tag(source.getName());
    }
}
