package com.jongsoft.finance.jpa.tag;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
@Named("tagProvider")
public class TagProviderJpa implements TagProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    public TagProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<Tag> lookup() {
        log.trace("Tag listing");

        var hql = """
                select t from TagJpa t
                where t.user.username = :username and t.archived = false""";

        return entityManager.<TagJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .sequence()
                .map(this::convert);
    }

    @Override
    public Maybe<Tag> lookup(String name) {
        log.trace("Tag lookup by name: {}", name);

        var hql = """
                select t from TagJpa t
                where t.name = :name
                    and t.user.username = :username
                    and t.archived = false""";

        return entityManager.<TagJpa>reactive()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", name)
                .maybe()
                .map(this::convert);
    }

    @Override
    public ResultPage<Tag> lookup(FilterCommand filter) {
        log.trace("Tag lookup by filter: {}", filter);

        if (filter instanceof TagFilterCommand delegate) {
            var offset = delegate.page() * delegate.pageSize();
            delegate.user(authenticationFacade.authenticated());

            return entityManager.<TagJpa>blocking()
                    .hql(delegate.generateHql())
                    .setAll(delegate.getParameters())
                    .limit(delegate.pageSize())
                    .offset(offset)
                    .sort(delegate.sort())
                    .page()
                    .map(this::convert);
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
