package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.lang.collection.Map;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

@Singleton
public class ReactiveEntityManager {
    private final EntityManager entityManager;

    ReactiveEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T extends EntityJpa> void persist(T entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else{
            entityManager.merge(entity);
        }
        entityManager.flush();
    }

    public <T> NonReactivePipe<T> blocking() {
        return new NonReactivePipe<T>(entityManager);
    }

    public <T> T getDetached(Class<T> type, Map<String, Object> filter) {
        var entity = getShared(type, filter);
        entityManager.detach(entity);
        return entity;
    }

    public <T> T get(Class<T> type, Map<String, Object> filter) {
        return getShared(type, filter);
    }

    private <T> T getShared(Class<T> type, Map<String, Object> filter) {
        String hql = "from " + type.getName() +
                filter.foldLeft(
                        " where 1 = 1",
                        (x, y) -> x + " AND " + y.getFirst() + " = :" + y.getFirst());
        var query = this.<T>blocking().hql(hql);
        filter.forEach(entry -> query.set(entry.getFirst(), entry.getSecond()));
        return query.maybe().get();
    }

    public UpdatingPipe update() {
        return new UpdatingPipe(entityManager);
    }

}
