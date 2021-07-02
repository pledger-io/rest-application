package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.lang.collection.Map;
import io.micronaut.transaction.SynchronousTransactionManager;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.sql.Connection;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ReactiveEntityManager {

    private final SynchronousTransactionManager<Connection> transactionManager;
    private final EntityManager entityManager;

    public <T extends EntityJpa> void persist(T entity) {
        transactionManager.executeWrite(status -> {
            if (entity.getId() == null) {
                entityManager.persist(entity);
            } else{
                entityManager.merge(entity);
            }
            entityManager.flush();
            return entity;
        });
    }

    public <T> ReactivePipe<T> reactive() {
        return new ReactivePipe<>(entityManager, transactionManager);
    }

    public <T> NonReactivePipe<T> blocking() {
        return new NonReactivePipe<T>(entityManager, transactionManager);
    }

    public <T> T getDetached(Class<T> type, Map<String, Object> filter) {
        return transactionManager.executeRead(status -> {
            var entity = getShared(type, filter);
            entityManager.detach(entity);
            return entity;
        });
    }

    public <T> T get(Class<T> type, Map<String, Object> filter) {
        return transactionManager.executeRead(status -> getShared(type, filter));
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
        return new UpdatingPipe(transactionManager, entityManager);
    }

}
