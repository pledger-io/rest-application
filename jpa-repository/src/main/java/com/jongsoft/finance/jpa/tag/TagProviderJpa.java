package com.jongsoft.finance.jpa.tag;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ReadOnly
@Singleton
@RequiresJpa
@Named("tagProvider")
public class TagProviderJpa implements TagProvider {

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
