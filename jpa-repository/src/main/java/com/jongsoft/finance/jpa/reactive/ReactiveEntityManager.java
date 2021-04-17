package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.lang.collection.Map;
import io.micronaut.transaction.SynchronousTransactionManager;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.sql.Connection;

@Singleton
public class ReactiveEntityManager {

    private final SynchronousTransactionManager<Connection> transactionManager;
    private final EntityManager entityManager;

    public ReactiveEntityManager(SynchronousTransactionManager<Connection> transactionManager, EntityManager entityManager) {
        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
    }

    public <T extends EntityJpa> void persist(T entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else{
            entityManager.merge(entity);
        }
    }

    public <T> ReactivePipe<T> reactive() {
        return new ReactivePipe<>(entityManager, transactionManager);
    }

    public <T> NonReactivePipe<T> blocking() {
        return new NonReactivePipe<T>(entityManager);
    }

    public <T> T getDetached(Class<T> type, Map<String, Object> filter) {
        var value = get(type, filter);
        entityManager.detach(value);
        return value;
    }

    public <T> T get(Class<T> type, Map<String, Object> filter) {
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
