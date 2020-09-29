package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.transaction.TagProvider;
import com.jongsoft.finance.jpa.core.DataProviderJpa;
import com.jongsoft.finance.jpa.transaction.entity.TagJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.SynchronousTransactionManager;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.sql.Connection;

@Slf4j
@Singleton
@Named("tagProvider")
public class TagProviderJpa extends DataProviderJpa<Tag, TagJpa> implements TagProvider {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;

    public TagProviderJpa(
            AuthenticationFacade authenticationFacade,
            EntityManager entityManager,
            SynchronousTransactionManager<Connection> transactionManager) {
        super(entityManager, TagJpa.class);
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<Tag> lookup() {
        log.trace("Tag listing");

        var hql = """
                select t from TagJpa t
                where t.user.username = :username and t.archived = false""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());

        return this.<TagJpa>multiValue(query)
                .map(this::convert);
    }

    @Override
    public Optional<Tag> lookup(String name) {
        log.trace("Tag lookup by name: {}", name);

        var hql = """
                select t from TagJpa t
                where t.name = :name
                    and t.user.username = :username
                    and t.archived = false""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("name", name);

        return API.Option(convert(singleValue(query)));
    }

    @Override
    public ResultPage<Tag> lookup(FilterCommand filter) {
        log.trace("Tag lookup by filter: {}", filter);

        if (filter instanceof TagFilterCommand delegate) {
            var offset = delegate.page() * delegate.pageSize();
            delegate.user(authenticationFacade.authenticated());

            return queryPage(
                    delegate,
                    API.Option(offset),
                    API.Option(delegate.pageSize()));
        }

        throw new IllegalStateException("Cannot use non JPA filter on TagProviderJpa");
    }

    @Override
    protected Tag convert(TagJpa source) {
        if (source == null) {
            return null;
        }

        return new Tag(source.getName());
    }

}
