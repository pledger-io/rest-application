package com.jongsoft.finance.jpa.reactive;

import io.micronaut.transaction.SynchronousTransactionManager;

import javax.persistence.EntityManager;
import java.sql.Connection;

public class UpdatingPipe extends JpaPipe<Void, UpdatingPipe> {

    private final SynchronousTransactionManager<Connection> transactionManager;
    private final EntityManager entityManager;

    public UpdatingPipe(SynchronousTransactionManager<Connection> transactionManager, EntityManager entityManager) {
        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
    }

    public void execute() {
        transactionManager.executeWrite(status -> {
            var query = entityManager.createQuery(hql());

            applyParameters(query);

            return query.executeUpdate();
        });
    }

    @Override
    protected UpdatingPipe self() {
        return this;
    }
}
