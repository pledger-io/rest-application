package com.jongsoft.finance.jpa.reactive;

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

    public <T> ReactivePipe<T> reactive() {
        return new ReactivePipe<>(entityManager, transactionManager);
    }

    public <T> NonReactivePipe<T> blocking() {
        return new NonReactivePipe<T>(entityManager);
    }

}
