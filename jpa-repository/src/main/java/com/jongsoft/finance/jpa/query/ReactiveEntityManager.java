package com.jongsoft.finance.jpa.query;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Map;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.collection.support.Collections;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.stream.Collector;
import lombok.Getter;

@Singleton
public class ReactiveEntityManager {
  @Getter
  private final EntityManager entityManager;

  private final AuthenticationFacade authenticationFacade;

  ReactiveEntityManager(EntityManager entityManager, AuthenticationFacade authenticationFacade) {
    this.entityManager = entityManager;
    this.authenticationFacade = authenticationFacade;
  }

  public <T extends EntityJpa> void persist(T entity) {
    if (entity.getId() == null) {
      entityManager.persist(entity);
    } else {
      entityManager.merge(entity);
    }
    entityManager.flush();
  }

  /**
   * Creates a JpaQuery instance for the specified entity type.
   *
   * @param type The entity class for which to create the query
   * @return A new JpaQuery instance configured with the provided entity type and entity manager
   */
  public <T> JpaQuery<T> from(Class<T> type) {
    return new JpaQuery<>(entityManager, type);
  }

  /**
   * Creates a JpaUpdate instance for the specified entity type.
   *
   * @param type The entity class for which to create the update
   * @return A new JpaUpdate instance configured with the provided entity type and entity manager
   */
  public <T> JpaUpdate<T> update(Class<T> type) {
    return new JpaUpdate<>(entityManager, type);
  }

  /**
   * Creates a new JpaQuery instance based on the provided JpaFilterBuilder.
   *
   * @param filterBuilder The JpaFilterBuilder used to construct the query
   * @return A new JpaQuery instance configured with the entity manager and entity type extracted
   *     from the filterBuilder
   */
  public <T> JpaQuery<T> from(JpaFilterBuilder<T> filterBuilder) {
    var query = new JpaQuery<>(entityManager, filterBuilder.entityType());
    filterBuilder.applyTo(query);
    return query;
  }

  public <T> T getDetached(Class<T> type, Map<String, Object> filter) {
    var queryBuilder = from(type);
    filter.forEach(e -> queryBuilder.fieldEq(e.getFirst(), e.getSecond()));
    var entity = queryBuilder.singleResult().get();
    entityManager.detach(entity);
    return entity;
  }

  public <T> T getById(Class<T> type, Long id) {
    return entityManager.find(type, id);
  }

  public UserAccountJpa currentUser() {
    return from(UserAccountJpa.class)
        .fieldEq("username", authenticationFacade.authenticated())
        .singleResult()
        .get();
  }

  /** Convert a stream to a sequence. */
  public static <T>
      Collector<T, ? extends ArrayList<T>, ? extends Sequence<T>> sequenceCollector() {
    return Collections.collector(com.jongsoft.lang.Collections::List);
  }
}
